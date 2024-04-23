"""
MIT License

Copyright (c) 2024 Andrew Baxter

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
"""

import os
import numpy as np
import sys
from PIL import Image

def open_image(image_path, size=(512, 512)):
    """
    Open an image, resize it, and return as a numpy array.
    """
    try:
        img = Image.open(image_path)
        if img.size != size:
            img = img.resize(size)
        img.save(image_path, 'PNG')  # Save the processed image
        return np.array(img)
    except Exception as e:
        print(f"Error opening or processing {image_path}: {e}")
        return None

def chi_square_statistic(image_path, target_size=(512, 512)):
    """
    Compute the chi-square statistic of the LSBs of an image (per channel for RGB/RGBA) to detect steganography.
    """
    try:
        pixels = open_image(image_path, size=target_size)
        if pixels is None:
            return None
        
        if pixels.ndim == 2:  # Grayscale image
            pixels = pixels.reshape((*pixels.shape, 1))  # Add channel dimension for uniform processing
        
        chi_square_stats = []
        for channel in range(pixels.shape[2]):
            lsb = np.bitwise_and(pixels[:,:,channel], 1)
            hist, _ = np.histogram(lsb.flatten(), bins=2, range=(0, 2))
            expected_freq = np.mean(hist)
            chi_square_stat = np.sum((hist - expected_freq) ** 2 / expected_freq)
            chi_square_stats.append(chi_square_stat)
        
        return chi_square_stats
    except Exception as e:
        print(f"Error processing {image_path}: {e}")
        return None

def detect_lsb_steganography(chi_square_stats, is_grayscale):
    """
    Determine if an image might contain steganography based on the chi-square statistic.
    Adjusts the threshold based on whether the image is grayscale or RGB/RGBA.
    """
    threshold = 10 if is_grayscale else 500
    return any(stat > threshold for stat in chi_square_stats)

def process_path(path):
    stego_count = 0
    total_images = 0
    
    if os.path.isdir(path):
        files = [os.path.join(path, f) for f in os.listdir(path) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]
        total_images = len(files)
        
        for file in files:
            img = Image.open(file)
            convert_grayscale = img.mode == 'L'
            chi_square_stats = chi_square_statistic(file)
            if chi_square_stats is not None:
                is_stego = detect_lsb_steganography(chi_square_stats, convert_grayscale)
                if is_stego:
                    stego_count += 1
                    print(f"File: {file}, Detected LSB Steganography: Yes, Chi-square Statistic: {max(chi_square_stats)}")
                else:
                    print(f"File: {file}, Detected LSB Steganography: No, Chi-square Statistic: {max(chi_square_stats)}")
        
        accuracy_percentage = (stego_count / total_images) * 100 if total_images > 0 else 0
        print(f"\nSteganography images found: {stego_count}")
        print(f"Total images: {total_images}")
        print(f"Accuracy percentage: {accuracy_percentage:.2f}%")

    elif os.path.isfile(path):
        img = Image.open(path)
        convert_grayscale = img.mode == 'L'
        chi_square_stats = chi_square_statistic(path)
        if chi_square_stats is not None:
            is_stego = detect_lsb_steganography(chi_square_stats, convert_grayscale)
            print(f"File: {path}, Detected LSB Steganography: {'Yes' if is_stego else 'No'}, Chi-square Statistic: {max(chi_square_stats)}")
    else:
        print("Invalid path.")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python script.py <path_to_image_or_folder>")
        sys.exit(1)
    
    path = sys.argv[1]
    process_path(path)