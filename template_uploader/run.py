from google.cloud import firestore

import os

# HAVIT: Google Firestore JSON Uploader
# Developed and Designed by John Seong, 2022
# ------------------------------------------
# 1. This code uploads the JSON object to Google Firestore, a NoSQL database
# 2. The JSON file that starts with "havitcentral-" contains the valuable API key that provides direct access to user information.
#    Do NOT expose it on a public repo!
# 3. VERY IMPORTANT STEP: Use GitHub Copilot to make the time stamping process easier! It will automatically generate the time stamping JSON for you.
# 4. To run this file, type "python run.py" in the terminal.

# Set up the credentials as an environmental variable
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "havitcentral-b63dac00aa76.json"

# Create a Firestore client
db = firestore.Client()

doc_ref = db.collection('templates').document('cherry-on-top')
doc_ref.set(
    {
        "name": "Cherry on Top",
        "description": "Like a Cherry, Your Spouse is the Star of the Show.",
        "price": 4.99,
        "music": "https://www.youtube.com/watch?v=2NVwrkj5GJo",
        "featured": False,
        "timestamp": {
            "00:05:00-00:07:00": "Walking in the forest...",
            "00:07:00-00:09:00": "You see a beautiful cherry tree.",
            "00:09:00-00:11:00": "You pick a cherry and eat it.",
            "00:11:00-00:13:00": "You hold out the cherry to your spouse.",
            "00:13:00-00:15:00": "Your spouse takes the cherry and eats it.",
            "00:15:00-00:17:00": "You kiss your spouse.",
            "00:17:00-00:19:00": "You hold your spouse's hand.",
            "00:19:00-00:21:00": "You and your spouse walk back to the house.",
            "00:21:00-00:23:00": "You and your spouse sit down on the couch.",
            "00:23:00-00:25:00": "You and your spouse watch TV.",
            "00:25:00-00:27:00": "You and your spouse cuddle.",
            "00:27:00-00:29:00": "You and your spouse fall asleep on the couch.",
            "00:29:00-00:31:00": "You and your spouse wake up on the couch.",
            "00:31:00-00:33:00": "You and your spouse go to bed."
        }
    }
)