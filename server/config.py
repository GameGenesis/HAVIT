#config.py
import firebase_admin
from firebase_admin import credentials, firestore, storage, auth

def initialize_app():
    global cred, db, bucket
    
    cred = credentials.Certificate("./assets/credentials.json")
    firebase_admin.initialize_app(cred, name='havit-api')


    db = firestore.client()
    bucket = storage.bucket('gs://havitcentral.appspot.com')
    
