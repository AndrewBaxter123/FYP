import os
import sys
import numpy as np
import requests
from PIL import Image
import rawpy
from joblib import load as joblib_load
import logging

# Setup basic logging
log_directory = os.path.join(os.environ.get('APPDATA'), 'SteganographyDetection')
os.makedirs(log_directory, exist_ok=True)  # Ensure the directory exists even if it already exists
log_file_path = os.path.join(log_directory, 'application.log')
logging.basicConfig(filename=log_file_path, level=logging.INFO, format='%(asctime)s:%(levelname)s:%(message)s')

def download_file(url, destination):
    """Download a file from a web URL to a local destination."""
    try:
        response = requests.get(url)
        response.raise_for_status()  # Raises an HTTPError for bad responses
        with open(destination, 'wb') as f:
            f.write(response.content)
        logging.info(f"Downloaded file from {url} to {destination}")
    except requests.RequestException as e:
        logging.error(f"Failed to download file from {url}: {str(e)}")
        sys.exit(1)

def setup_application():
    """Set up application by ensuring all necessary files are present."""
    # Set the URLs to the direct download links of the raw files on GitHub
    model_url = 'https://github.com/AndrewBaxter123/FYP/raw/main/src/org/andrewbaxter/SteganographyDetection/models/SVC_model.pkl'
    scaler_url = 'https://github.com/AndrewBaxter123/FYP/raw/main/src/org/andrewbaxter/SteganographyDetection/models/scaler.joblib'
    local_app_data = os.environ.get('LOCALAPPDATA', os.path.join(os.environ.get('USERPROFILE'), 'AppData', 'Local'))
    model_dir = os.path.join(local_app_data, 'models')

    if not os.path.exists(model_dir):
        os.makedirs(model_dir)
        logging.info(f"Created directory at {model_dir}")

    model_path = os.path.join(model_dir, 'SVC_model.pkl')
    scaler_path = os.path.join(model_dir, 'scaler.joblib')

    # Download model and scaler files if they don't exist locally
    if not os.path.exists(model_path):
        download_file(model_url, model_path)
    if not os.path.exists(scaler_path):
        download_file(scaler_url, scaler_path)

    return model_path, scaler_path


model_path, scaler_path = setup_application()

try:
    model = joblib_load(model_path)
    scaler = joblib_load(scaler_path)
except Exception as e:
    logging.error(f"Failed to load model or scaler: {e}")
    sys.exit(1)

def process_dng(image_path):
    try:
        with rawpy.imread(image_path) as raw:
            rgb = raw.postprocess()
        return Image.fromarray(rgb)
    except Exception as e:
        logging.error(f"Error processing DNG {image_path}: {e}")
        return None

def spam_extract_2(X, T):
    """Extract features using a spam method."""
    def getM3(L, C, R, T):
        L = np.clip(L, -T, T)
        C = np.clip(C, -T, T)
        R = np.clip(R, -T, T)
        M = np.zeros((2*T+1, 2*T+1, 2*T+1))
        for i in range(-T, T+1):
            for j in range(-T, T+1):
                for k in range(-T, T+1):
                    M[i+T, j+T, k+T] = np.sum((L == i) & (C == j) & (R == k))
        return M.flatten() / np.sum(M)
    
    D1, D2, D3, D4 = X[:, :-1] - X[:, 1:], X[:-1, :] - X[1:, :], X[:-1, :-1] - X[1:, 1:], X[1:, :-1] - X[:-1, 1:]
    features = [getM3(D[:-2].flatten(), D[1:-1].flatten(), D[2:].flatten(), T) for D in [D1, D2, D3, D4]]
    return np.mean(features, axis=0)

def preprocess_and_extract_features(image_path, T=3):
    """Preprocess and extract features from an image."""
    try:
        if image_path.lower().endswith('.dng'):
            img = process_dng(image_path)
        else:
            img = Image.open(image_path).convert('L')
        
        img_resized = img.resize((512, 512), Image.LANCZOS)
        img_array = np.array(img_resized).astype(np.float32)
        features = spam_extract_2(img_array, T)
        logging.info("Features extracted successfully.")
        return features
    except Exception as e:
        logging.error(f"Error in preprocessing {image_path}: {e}")
        return None

def predict_image(image_path):
    """Predict if an image contains steganography."""
    try:
        features = preprocess_and_extract_features(image_path)
        if features is not None:
            features_scaled = scaler.transform(features.reshape(1, -1))
            prediction = model.predict(features_scaled)
            return prediction[0]
        else:
            logging.error("Features could not be extracted.")
            return "Error"
    except Exception as e:
        logging.error(f"Error during prediction: {e}")
        return "Error"

if __name__ == "__main__":
    if len(sys.argv) != 2:
        logging.error("ERROR: Incorrect usage, expected a single image path as argument.")
        sys.exit(1)

    image_path = sys.argv[1]
    prediction = predict_image(image_path)
    if prediction == "Error":
        logging.error("ERROR: Could not process image.")
    else:
        result = "Yes" if prediction == 1 else "No"
        logging.info(f"Steganography Detected: {result}")
        print(f"Steganography Detected: {result}")
