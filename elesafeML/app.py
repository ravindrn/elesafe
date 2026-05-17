from flask import Flask, request, jsonify
import joblib
import pandas as pd

app = Flask(__name__)

print("Loading Machine Learning Model...")
# 1. Load the AI Brain and the exact column structure it expects
rf_model = joblib.load('random_forest_model.pkl')
model_columns = joblib.load('model_columns.pkl')
print("Model loaded successfully. API is ready.")

@app.route('/predict_risk', methods=['POST'])
def predict_risk():
    try:
        # 2. Receive the JSON payload from your Java App
        incoming_data = request.get_json()
        
        # 3. Convert the JSON into a Pandas DataFrame
        input_df = pd.DataFrame([incoming_data])
        
        # 4. Preprocess: Turn the text ("Night", "Clear") into numbers (One-Hot Encoding)
        input_encoded = pd.get_dummies(input_df, columns=['Time_of_Day', 'Weather'])
        
        # 5. Align Columns: The app might send "Night", but the model expects columns for
        # Morning, Afternoon, Dusk, and Night. This line fills missing columns with 0.
        input_final = input_encoded.reindex(columns=model_columns, fill_value=0)
        
        # 6. Make the Prediction
        prediction = rf_model.predict(input_final)[0]
        
        # 7. Send the prediction back to the Java app
        return jsonify({
            "status": "success",
            "predicted_risk": prediction
        })

    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 400

if __name__ == '__main__':
    # Run the server on port 5000
    app.run(debug=True, host='0.0.0.0', port=5000)