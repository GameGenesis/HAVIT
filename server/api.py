from flask import Flask, request
from flask_cors import CORS

import firebase_admin
from firebase_admin import auth, credentials

from video import export_video

cred = credentials.Certificate("./assets/credentials.json")
firebase_admin.initialize_app(cred)

app = Flask(__name__, static_folder='../web/build', static_url_path='/')

CORS(bp, resources={r'/api/*': {'origins': '*'}})

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

@app.route('/api/firebase-auth', methods=['POST'])
@cross_origin(origin='*', headers=['Content-Type', 'Authorization'])
def firebase_auth():
    # Get the Firebase credentials from the request
    firebase_token = request.form['firebase_token']

    # Verify the Firebase token using the Firebase Admin SDK
    try:
        decoded_token = auth.verify_id_token(firebase_token)
        print("Successfully verified Firebase token: ", decoded_token);

        return {'status': 'success', 'user_id': decoded_token['uid']}, 200

    except auth.AuthError as e:
        return {'status': 'error', 'message': str(e)}, 400

@app.route('/api/export-video', methods=['POST'])
@cross_origin
def get_video():
    timeline_name = request.form['timeline_name']
    template_name = request.form['template_name']

    user = auth.get_user(user_id)
    email = user.email

    try:
        export_video(email, timeline_name, template_name)

        return {'status': 'success'}, 200
    
    except:
        return {'status': 'error'}, 400

if __name__ == '__main__':
    # python api.py (Windows) OR python3 api.py (macOS/Linux)
    app.run()