# importing libraries
import os
import cv2 
from PIL import Image 
from io import BytesIO
from flask import current_app
import firebase_admin
from firebase_admin import firestore, storage, auth, credentials

# Function to generate video
# For the reference, check out:
# https://www.geeksforgeeks.org/python-create-video-using-multiple-images-using-opencv/

def export_video(firebase_token, timeline_name, template_name):
    cred = credentials.Certificate("assets/credentials.json")
    firebase_admin.initialize_app(cred, name='havit-api')

    decoded_token = auth.verify_id_token(firebase_token)

    user = auth.get_user(user_id)
    user_email = user.email

    current_app.logger.info(f'Email: {user_email}')

    db = firestore.client()
    bucket = storage.bucket('gs://havitcentral.appspot.com')

    # Get the images from the specified folder in the storage bucket
    image_files = bucket.get_files({ prefix: 'users/' + user_email + '/' + timeline_name + '/' })
    images = asyncio.gather(*(file.download() for file in image_files))

    global mean_width, mean_height
    
    mean_height = 0
    mean_width = 0
    
    num_of_images = len(images)

    for image_data in images:
        image = Image.open(BytesIO(image_data))
        width, height = image.size
        mean_width += width
        mean_height += height

    # Finding the mean height and width of all images.
    # This is required because the video frame needs
    # to be set with same width and height. Otherwise
    # images not equal to that width height will not get 
    # embedded into the video

    mean_width = int(mean_width / num_of_images)
    mean_height = int(mean_height / num_of_images)

    # Resizing of the images to give
    # them same width and height 
    for image_data in images:
        file = BytesIO(image_data)

        if file.endswith(".jpg") or file.endswith(".jpeg") or file.endswith("png"):
            # opening image using PIL Image
            im = Image.open(file)
    
            # im.size includes the height and width of image
            width, height = im.size   
            print(width, height)
    
            # resizing 
            imResize = im.resize((mean_width, mean_height), Image.ANTIALIAS) 

            # Convert the modified image back to a BytesIO object
            imResize.save( file, 'JPEG', quality = 95) # setting quality

            image_data = file.read()

    # Calling the generate_video function
    generate_video(images, user_email, timeline_name, template_name)

# Video Generating function
def generate_video(images, user_email, timeline_name, template_name, fps=30):
    video_name = f'./temp/{user_email}/{timeline_name}.avi'

    frame = cv2.imread(BytesIO(images[0]))
  
    # setting the frame width, height width
    # the width, height of first image
    height, width, layers = frame.shape  
  
    # Define the codec and create VideoWriter object
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    video = cv2.VideoWriter(video_name, fourcc, float(fps), (width, height)) 

    duration = get_data_from_firestore(template_name)

    previous_end_time = 0;
    
    # Appending the images to the video one by one
    for i, image in enumerate(images): 
        # Duration is in milliseconds, so we need to convert it to seconds
        current_start_time = duration[i][0] / 1000
        current_end_time = duration[i][1] / 1000

        # Create a new image with a size of (300, 300)
        black_image = Image.new('RGB', (mean_width, mean_height), (0, 0, 0))

        # Save the image as an object in memory
        black_image_object = image.tobytes()

        if (current_start_time - previous_end_time) > 0:
            # Add a gap between the previous frame and the current frame
            for _ in range(int(fps * (current_start_time - previous_end_time))):
                # Add a black frame
                video.write(cv2.imread(BytesIO(black_image_object)))
        
        for _ in range(int(fps * current_end_time - current_start_time)):
            # Repeat each frame for the specified duration
            video.write(cv2.imread(BytesIO(image)))
      
    # Deallocating memories taken for window creation
    cv2.destroyAllWindows() 
    video.release()  # releasing the video generated

    current_app.logger.info("users/" + user_email + "/" + timeline_name + "/export.avi")

    # Create a reference to the video in Firebase Storage
    video_blob = bucket.blob("users/" + user_email + "/" + timeline_name + "/export.avi")

    # Open the video file
    with open(video_name, "rb") as video_file:
        # Upload the video to Firebase Storage
        video_blob.upload_from_file(video_file)

def get_data_from_firestore(template_name):
    doc_ref = db.collection('templates').document(template_name)

    doc = doc_ref.get()

    duration = []

    if doc.exists:
        # Iterate through the map field
        for key, value in doc.to_dict().get("timestamp").items():
            key_list = key.split("-")

            start_time_str = key_list[0]
            start_millis = int(start_time_str[0:2]) * 60000 + int(start_time_str[2:4]) * 1000 + int(start_time_str[4:6])
            
            end_time_str = key_list[1]
            end_millis = int(end_time_str[0:2]) * 60000 + int(end_time_str[2:4]) * 1000 + int(end_time_str[4:6])

            duration.append([start_millis, end_millis])

    return duration
            
# TO DO: ADD MEMBERSHIP_ENABLED BOOLEAN TO FIRESTORE USER.JSON AS WELL AS FIRST_TIME_USER BOOLEAN
