import os
import numpy as np
from PIL import Image
import rawpy
from joblib import load as joblib_load
import imageio.v2 as imageio
import sys

model_path = os.path.join(os.path.dirname(__file__), '..', 'models', 'SVC_model.pkl')
scaler_path = os.path.join(os.path.dirname(__file__), '..', 'models', 'scaler.joblib')


print(f"Model path: {model_path}")
model = joblib_load(model_path)

scaler = joblib_load(scaler_path)

def process_dng(image_path):
    try:
        with rawpy.imread(image_path) as raw:
            rgb = raw.postprocess()
        return Image.fromarray(rgb)
    except Exception as e:
        print(f"Error processing DNG {image_path}: {e}")
        return None
    
def spam_extract_2(X, T):
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
    
    def computeDifferences(X):
        D1 = X[:, :-1] - X[:, 1:]
        D2 = X[:-1, :] - X[1:, :]
        D3 = X[:-1, :-1] - X[1:, 1:]
        D4 = X[1:, :-1] - X[:-1, 1:]
        return D1, D2, D3, D4
    
    features = []
    D1, D2, D3, D4 = computeDifferences(X)
    for D in [D1, D2, D3, D4]:
        L = D[:-2]; C = D[1:-1]; R = D[2:]
        features.append(getM3(L.flatten(), C.flatten(), R.flatten(), T))
    F = np.mean(features, axis=0)
    return F

def preprocess_and_extract_features(image_path, T=3):
    try:
        if image_path.lower().endswith('.dng'):
            img = process_dng(image_path)
        else:
            img = Image.open(image_path).convert('L')
        
        img_resized = img.resize((512, 512), Image.LANCZOS)
        img_array = np.array(img_resized).astype(np.float32)
        
        features = spam_extract_2(img_array, T)  # Extract features directly from the numpy array
        print("Features extracted successfully.")
        return features
    except Exception as e:
        print(f"Error in preprocessing {image_path}: {e}")
        return None
    


def predict_image(image_path):
    features = preprocess_and_extract_features(image_path)
    if features is not None:
        try:
            features_scaled = scaler.transform(features.reshape(1, -1))
            prediction = model.predict(features_scaled)
            return prediction[0]
        except Exception as e:
            return "Error"
    else:
        return "Error"

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("ERROR: Usage: python svc_predict.py <path_to_image>")
        sys.exit(1)
    
    image_path = sys.argv[1]
    prediction = predict_image(image_path)
    if prediction == "Error":
        print("ERROR: Could not process image.")
    else:
        print("Steganography Detected: Yes" if prediction == 1 else "Steganography Detected: No")
