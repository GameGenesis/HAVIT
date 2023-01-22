from firebase_admin import firestore, storage, auth, credentials

import os, cv2, firebase_admin

from PIL import Image 
from io import BytesIO

# Set up the credentials as an environmental variable
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "havitcentral-b63dac00aa76.json"

default_app = firebase_admin.initialize_app()

def export_video(firebase_token, timeline_name, template_name, fps=30):
    '''
    This function is used to export a video from the images in the Firebase Storage Bucket

    Parameters
    ----------
    firebase_token: String
        The Firebase token of the user
    timeline_name: String
        The name of the timeline
    template_name: String
        The name of the template
    fps: Integer
        The frames per second of the video

    Returns
    -------
    None
    '''

    decoded_token = auth.verify_id_token(firebase_token)
    user_id = decoded_token['uid']

    user = auth.get_user(user_id)
    user_email = user.email

    # VVIP: Do NOT write gs:// in front of the bucket name
    bucket = storage.bucket('havitcentral.appspot.com')

    # Get the images from the specified folder in the storage bucket
    blobs = bucket.list_blobs(prefix='users/' + user_email + '/' + timeline_name + '/')

    image_files = []

    for blob in blobs:
        if blob.content_type.startswith('image'):
            image_files.append(blob)

    # Create a directory to store the images
    if not os.path.exists(f'./temp/{user_email}/{timeline_name}/'):
        os.makedirs(f'./temp/{user_email}/{timeline_name}/')
    
    # Download the images to the local directory
    for index, blob in enumerate(image_files):
        blob.download_to_filename(f'./temp/{user_email}/{timeline_name}/IMG_{index}')

    mean_height = 0
    mean_width = 0
    
    num_of_images = len(image_files)

    for index in range(num_of_images):
        image = Image.open(f'./temp/{user_email}/{timeline_name}/IMG_{index}')
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

    # Resizing of the images to give them same width and height 
    for index in range(num_of_images):
        im = Image.open(f'./temp/{user_email}/{timeline_name}/IMG_{index}')
    
        width, height = im.size   
    
        # resizing 
        imResize = im.resize((mean_width, mean_height), Image.ANTIALIAS)

        # Saving the resized image
        imResize.save(f'./temp/{user_email}/{timeline_name}/IMG_{index}', 'JPEG', quality = 95)

    video_name = f'./temp/{user_email}/{timeline_name}.avi'

    # Define the codec and create VideoWriter object
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter(video_name, fourcc, fps, (mean_width, mean_height))
    duration = get_data_from_firestore(template_name)

    previous_end_time = 0;
    
    # Appending the images to the video one by one
    for i in range(num_of_images):
        # Duration is in milliseconds, so we need to convert it to seconds
        current_start_time = duration[i][0] / 1000
        current_end_time = duration[i][1] / 1000

        # Create a new image with a size of (300, 300)
        black_image = Image.new('RGB', (mean_width, mean_height), (0, 0, 0))

        if (current_start_time - previous_end_time) > 0:
            # Add a gap between the previous frame and the current frame
            for _ in range(int(fps * (current_start_time - previous_end_time))):
                # Add a black frame
                gap = cv2.imread(BytesIO(black_image.tobytes(), format='PNG'))
                out.write(gap)
        
        for _ in range(int(fps * current_end_time - current_start_time)):
            # Repeat each frame for the specified duration
            img_path = f'./temp/{user_email}/{timeline_name}/IMG_{index}'
            frame = cv2.imread(img_path)

            # Writing the extracted images
            out.write(frame)


    # Deallocating memories taken for window creation
    cv2.destroyAllWindows() 
    out.release()

    # Upload the video file to the storage bucket
    video_blob = bucket.blob(f'users/{user_email}/{timeline_name}.avi')
    video_blob.upload_from_filename(video_name)

    # Delete the temp images
    for index in range(num_of_images):
        os.remove(f'./temp/{user_email}/{timeline_name}/IMG_{index}')

    # delete temp folder
    os.remove(f'./temp/{user_email}/{timeline_name}.avi')


def get_data_from_firestore(template_name):
    '''
    Get the data from the firestore database
    
    Parameters:
        template_name (str): The name of the template

    Returns:
        duration (list): A list of tuples containing the start and end time of each frame
    '''
    db = firestore.client()
    doc_ref = db.collection('templates').document(template_name)

    doc = doc_ref.get()

    duration = []

    if doc.exists:
        # Iterate through the map field
        for key, value in doc.to_dict().get("timestamp").items():
            key_list = key.split("-")

            start_time_array = key_list[0].split(":")
            start_time_minute = int(start_time_array[0]) * 60
            start_time_second = int(start_time_array[1])
            start_time_millis = int(start_time_array[2]) * 100

            start_millis = (start_time_minute + start_time_second) * 1000 + start_time_millis
            
            end_time_array = key_list[1].split(":")
            end_time_minute = int(end_time_array[0]) * 60
            end_time_second = int(end_time_array[1])
            end_time_millis = int(end_time_array[2])

            end_millis = (end_time_minute + end_time_second) * 1000 + end_time_millis

            duration.append([start_millis, end_millis])

    return duration