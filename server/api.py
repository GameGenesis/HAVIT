from flask import Flask, request
from flask_cors import CORS, cross_origin

import os

# SERVER-SIDE CODE OF THE HAVIT APP

# from rq import Queue

# from worker import conn
# from utils import export_video

# q = Queue(connection=conn)

app = Flask(__name__, static_folder='../web/build', static_url_path='/')

app.config["SECRET_KEY"] = os.environ.get("SECRET_KEY")

CORS(app, resources={r'/api/*': {'origins': '*'}})

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# For Flask to React Routing...
@app.errorhandler(404)
def not_found(e):   
    '''
    This function is used to redirect the user to the React Router page

    Parameters
    ----------
    e: Exception
        The exception that was raised

    Returns
    -------
    DOM File
        Returns a HTML script that contains the visual elements of the website
    '''
    if request.method == 'GET':
        return app.send_static_file('index.html')
    else:
        return {'status': 'error', 'message': 'Endpoint not found'}, 404

@app.route('/') 
def serve():
    '''
    This function is executed in root directory,
    redirecting to the static HTML file generated by React front-end framework
    
    Parameters
    ----------
    None

    Returns
    -------
    DOM File
        Returns a HTML script that contains the visual elements of the website
    '''
    if request.method == 'GET':
        return app.send_static_file('index.html')
    else:
        return {'status': 'error', 'message': 'Endpoint not found'}, 404

@app.route('/api/export-video', methods=['POST'])
@cross_origin()
def get_video():
    '''
    This function is used to export a video from the images in the Firebase Storage Bucket
    
    Parameters
    ----------
    None

    Returns
    -------
    JSON
        Returns a JSON object with the status of the video export
    '''
    timeline_name = request.form['timeline_name']
    template_name = request.form['template_name']
    firebase_token = request.form['firebase_token']

    try:
        # result = q.enqueue(export_video, firebase_token, timeline_name, template_name, result_ttl=5000)
        return {'status': 'success'}, 200

    except Exception as e:
        app.logger.error(e)
        return {'status': 'error'}, 400

if __name__ == '__main__':
    # python api.py (Windows) OR python3 api.py (macOS/Linux)
    port = int(os.environ.get("PORT", 5000)) 
    
    app.run(host='0.0.0.0', port=port)