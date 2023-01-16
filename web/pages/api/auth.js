import firebase_admin from 'firebase-admin';

const cred = JSON.parse(process.env.FIREBASE_CREDENTIALS);

firebase_admin.initializeApp({
    credential: firebase_admin.credential.cert(cred),
});

export default (req, res) => {
    try {
        const decoded_token = firebase_admin.auth().verifyIdToken(req.body.firebase_token);
        console.log("Successfully verified Firebase token: ", decoded_token);

        res.status(200).json({status: 'success', user_id: decoded_token.uid});
    } catch (error) {
        console.log("Error while verifying Firebase token: ", error);

        res.status(400).json({status: 'error', message: error.message});
    }
};