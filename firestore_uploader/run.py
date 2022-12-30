from google.cloud import firestore

import os

# HAVIT: Google Firestore JSON Uploader
# Developed and Designed by John Seong, 2022
# ------------------------------------------
# 1. This code uploads the JSON object to Google Firestore, a NoSQL database
# 2. The JSON file that starts with "havitcentral-" contains the valuable API key that provides direct access to user information.
#    Do NOT expose it on a public repo!

# Set up the credentials as an environmental variable
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "havitcentral-b63dac00aa76.json"

# Create a Firestore client
db = firestore.Client()

doc_ref = db.collection('templates').document('evolution')
doc_ref.set(
    {
        "name": "Evolution",
        "description": "Your baby photos! :D",
        "price": 0,
        "music": "https://www.youtube.com/watch?v=Q0oIoR9mLwc",
        "image": "https://i.imgur.com/8ZQZ1Zm.png",
        "featured": False,
        "timestamp": {
            "00:05:00-00:07:00": "Year 0",
            "00:07:00-00:09:00": "Year 1",
            "00:09:00-00:11:00": "Year 2",
            "00:11:00-00:13:00": "Year 3",
            "00:13:00-00:15:00": "Year 4",
            "00:15:00-00:17:00": "Year 5",
            "00:17:00-00:19:00": "Year 6",
            "00:19:00-00:21:00": "Year 7",
            "00:21:00-00:23:00": "Year 8",
            "00:23:00-00:25:00": "Year 9",
            "00:25:00-00:27:00": "Year 10",
            "00:27:00-00:29:00": "Year 11",
            "00:29:00-00:31:00": "Year 12",
            "00:31:00-00:33:00": "Year 13",
            "00:33:00-00:35:00": "Year 14",
            "00:35:00-00:37:00": "Year 15",
            "00:37:00-00:39:00": "Year 16",
            "00:39:00-00:41:00": "Year 17",
            "00:41:00-00:43:00": "Year 18",
            "00:43:00-00:45:00": "Year 19"
        }
    }
)