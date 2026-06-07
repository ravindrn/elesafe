from flask import Flask, request, jsonify
import joblib
import pandas as pd
import os

app = Flask(__name__)

# 1. Load your trained Random Forest model
MODEL_PATH = "elephant_rf_model.pkl"

try:
    if os.path.exists(MODEL_PATH):
        model = joblib.load(MODEL_PATH)
        print("✅ Success: ML Model loaded perfectly!")
    else:
        model = None
        print("⚠️ Warning: elephant_rf_model.pkl not found. Running in fallback mode.")
except Exception as e:
    model = None
    print(f"❌ Error loading model: {e}")

# 2. Dictionary to convert text from Java into Numbers for the ML Model
# (Adjust these numbers if you used different encoding in Google Colab!)
TIME_ENCODING = {"Morning": 0, "Afternoon": 1, "Dusk": 2, "Night": 3}
WEATHER_ENCODING = {"Clear": 0, "Light Rain": 1, "Heavy Rain": 2}

@app.route('/predict_risk', methods=['POST'])
def predict_risk():
    try:
        # A. Receive the JSON data from Spring Boot
        data = request.get_json()
        
        distance = data.get('Distance_to_Zone_km', 20.0)
        elephant_count = data.get('Recent_Elephant_Count', 0)
        time_of_day_text = data.get('Time_of_Day', 'Morning')
        weather_text = data.get('Weather', 'Clear')

        print(f"📥 Received Request - Dist: {distance}, Count: {elephant_count}, Time: {time_of_day_text}, Weather: {weather_text}")

        # B. If you haven't moved your .pkl file yet, use this smart fallback logic to test!
        if model is None:
            print("Using fallback logic (No ML Model found)...")
            risk = "LOW"
            if elephant_count >= 5:
                risk = "CRITICAL"
            elif elephant_count > 0 or distance < 2.0:
                risk = "HIGH"
            
            return jsonify({
                "status": "success",
                "predicted_risk": risk,
                "message": "Fallback prediction (missing .pkl)"
            })

        # C. Encode the text into numbers for the Random Forest
        time_encoded = TIME_ENCODING.get(time_of_day_text, 0)
        weather_encoded = WEATHER_ENCODING.get(weather_text, 0)

        # D. Format the data exactly how Scikit-Learn expects it
        features = pd.DataFrame([{
            'Distance_to_Zone_km': distance,
            'Recent_Elephant_Count': elephant_count,
            'Time_of_Day': time_encoded,
            'Weather': weather_encoded
        }])

        # E. Ask the Model for a prediction!
        # The model returns an array like ['CRITICAL'], so we grab the first item
        prediction_result = model.predict(features)
        
        print(f"🧠 AI Predicts: {prediction_result}")

        # F. Send the answer back to Spring Boot!
        return jsonify({
            "status": "success",
            "predicted_risk": str(prediction_result)
        })

    except Exception as e:
        print(f"❌ API Error: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500


# Start the server on Port 5000
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)