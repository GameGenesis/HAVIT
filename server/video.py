# importing libraries
import os
import cv2 
from PIL import Image 
from firebase_admin import storage, firestore
from io import BytesIO

# Create a reference to the Firebase Storage bucket
bucket = storage.bucket('gs://havitcentral.appspot.com')

# https://www.geeksforgeeks.org/python-create-video-using-multiple-images-using-opencv/
async def export_video(user_email, timeline_name, template_name):
    # UNFINISHED LOGIC: define duration (seconds) for each image - List[float]
    # Get the images from the specified folder in the storage bucket
    image_files = await bucket.get_files({ prefix: 'users/' + user_email + '/' + timeline_name + '/' })
    images = await asyncio.gather(*(file.download() for file in image_files))

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
    generate_video(images, user_email, timeline_name, duration)

# Video Generating function
def generate_video(images, user_email, timeline_name, duration, fps=30):
    video_name = f'./temp/{user_email}/{timeline_name}.avi'

    frame = cv2.imread(BytesIO(images[0]))
  
    # setting the frame width, height width
    # the width, height of first image
    height, width, layers = frame.shape  
  
    # Define the codec and create VideoWriter object
    fourcc = cv2.VideoWriter_fourcc(*"MP42")
    video = cv2.VideoWriter(video_name, fourcc, float(fps), (width, height)) 

    # Appending the images to the video one by one
    for i, image in enumerate(images): 
        for _ in range(int(fps * duration[i])):  # Repeat each frame for the specified duration
            video.write(cv2.imread(BytesIO(image)))
      
    # Deallocating memories taken for window creation
    cv2.destroyAllWindows() 
    video.release()  # releasing the video generated

    # Create a reference to the video in Firebase Storage
    video_blob = bucket.blob("users/" + user_email + "/" + timeline_name + "/" + video_name)

    # Open the video file
    with open(video_name, "rb") as video_file:
        # Upload the video to Firebase Storage
        video_blob.upload_from_file(video_file)

def get_data_from_firestore(template_name):
    # Get a reference to the Firestore database
    # Unfinished logic; retrieve timestamp data from Firestore
    db = firestore.client()

    doc_ref = db.collection('templates').document(template_name)
 
