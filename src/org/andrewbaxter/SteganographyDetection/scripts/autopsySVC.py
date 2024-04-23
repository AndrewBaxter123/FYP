import os
import sys
import numpy as np
from PIL import Image
import rawpy
from joblib import load as joblib_load
import shutil
import logging

# Setup basic logging
log_directory = os.path.join(os.environ.get('APPDATA'), 'SteganographyDetection')
if not os.path.exists(log_directory):
    os.makedirs(log_directory)  # Ensure the directory exists
log_file_path = os.path.join(log_directory, 'application.log')
logging.basicConfig(filename=log_file_path, level=logging.INFO, format='%(asctime)s:%(levelname)s:%(message)s')

def setup_application():
    user_local_app_data = os.environ.get('LOCALAPPDATA', os.path.join(os.environ.get('USERPROFILE'), 'AppData', 'Local'))
    model_dir = os.path.join(user_local_app_data, 'models')

    if not os.path.exists(model_dir):
        os.makedirs(model_dir)
        logging.info(f"Created directory at {model_dir}")

    source_model = r'C:\Users\andre\Dropbox\My PC (LAPTOP-50HMEA5E)\Documents\4thyearCollege\Sem2Project\clone\FYP\src\org\andrewbaxter\SteganographyDetection\models\SVC_model.pkl'
    destination_model = os.path.join(model_dir, 'SVC_model.pkl')

    source_scaler = r'C:\Users\andre\Dropbox\My PC (LAPTOP-50HMEA5E)\Documents\4thyearCollege\Sem2Project\clone\FYP\src\org\andrewbaxter\SteganographyDetection\models\scaler.joblib'
    destination_scaler = os.path.join(model_dir, 'scaler.joblib')

    shutil.copy(source_model, destination_model)
    shutil.copy(source_scaler, destination_scaler)
    logging.info("Model and scaler files have been copied successfully.")

setup_application()

base_dir = os.environ.get('LOCALAPPDATA', os.path.join(os.environ.get('USERPROFILE', ''), 'AppData', 'Local'))
model_dir = os.path.join(base_dir, 'models')
model_path = os.path.join(model_dir, 'SVC_model.pkl')
scaler_path = os.path.join(model_dir, 'scaler.joblib')

if not os.path.exists(model_dir):
    os.makedirs(model_dir)
    logging.info("Created directory at {model_dir}")

logging.info(f"Model path: {model_path}")
logging.info(f"Scaler path: {scaler_path}")

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
        logging.info("Features extracted successfully.")
        return features
    except Exception as e:
        logging.error(f"Error in preprocessing {image_path}: {e}")
        return None

def predict_image(image_path):
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
