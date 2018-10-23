import numpy as np
import os

from predict_client.prod_client import ProdClient
from flask import Flask
from flask import request
from flask import jsonify
from flask_caching import Cache
from flask import send_file, send_from_directory

import base64
from PIL import Image
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import time

import sys
import tensorflow as tf

import cv2
import io
import scipy

## Adding util functions from tensorflow/models : https://github.com/tensorflow/models
## It's necessary for scaling the masks to sizes of the input image
sys.path.append("/home/k33p/anaconda3/lib/python3.6/site-packages/tensorflow/models/research/") 
from object_detection.utils import ops as utils_ops

app = Flask(__name__)
# This is the path to the upload directory
uploadspath = '/uploads/'
app.config['UPLOAD_FOLDER'] = '/home/k33p/Downloads/tfserving_exp/tfserving-python-predict-client/' + uploadspath
# These are the extension that we are accepting to be uploaded
app.config['ALLOWED_EXTENSIONS'] = set(['txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'])

SEG_HOST = 'localhost:9000'
SEG_MODEL_NAME = 'model'
SEG_MODEL_VERSION = 1
SEG_MODEL_IMAGE_INPUT = 'inputs'
SEG_MODEL_IMAGE_TENSOR_TYPE = 'DT_UINT8' 

INP_HOST = 'localhost:9001'
INP_MODEL_NAME = 'model2'
INP_MODEL_VERSION = 1
INP_MODEL_IMAGE_INPUT = 'input_3:0'
INP_MODEL_MASK_INPUT = 'input_4:0'
INP_MODEL_IMAGE_TENSOR_TYPE = 'DT_FLOAT'
INP_MODEL_MASK_TENSOR_TYPE = 'DT_FLOAT'
INP_MODEL_MASK_OUTPUT = 'conv2d_1/Sigmoid:0'

clientSegmentation = ProdClient(SEG_HOST, SEG_MODEL_NAME, SEG_MODEL_VERSION)
clientInpainting = ProdClient(INP_HOST, INP_MODEL_NAME, INP_MODEL_VERSION)

cache = Cache(app, config={'CACHE_TYPE': 'simple'})

# This route is expecting a parameter containing the name
# of a file. Then it will locate that file on the upload
# directory and show it on the browser, so if the user uploads
# an image, that image is going to be show after the upload
#TODO add XSendfile + Nginx
@app.route('/uploads/<path:filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)


@app.route("/inpainting", methods=['POST'])
def get_inpainting():
    req_data = request.files
    data = req_data['data']
    photo = convert_data(data)
    
    maskData = req_data['mask']
    mask = convert_data(maskData)

    mask, photo = mask_and_photo_normalization(mask, photo)
    
    ## Interfer the model
    start_time = time.time()
    tensor_dict = get_inpainting_from_model(photo, mask)
    print("Inpainting time: %s seconds" % (time.time() - start_time))
    pic = tensor_dict[INP_MODEL_MASK_OUTPUT][0]
    plt.imsave('pic'+ '.png', pic)

    return send_file(
        'pic.png',
        mimetype='image/png',
        attachment_filename='pic.png',
        cache_timeout=0
    )  

@app.route("/segmentation", methods=['POST'])
def get_segmentation():
    req_data = request.files
    photo = request.files['data']
    data = convert_data(photo)
    print("Input tensor dims: ", data.shape)
    filename = photo.filename
    maskpath = create_path_if_its_not_exist(filename)

    ## Interfer the model
    start_time = time.time()
    tensor_dict = get_segmentation_from_model(data)
    print("Segmentation time: %s seconds" % (time.time() - start_time))

    masks, boxes = scale_masks_to_image_size(tensor_dict, data.shape[1], data.shape[2])
    masks_dict, boxes_dict = get_masks_and_boxes_dicts(maskpath, filename, masks, boxes)
    masks_dict['mask_sum'] = get_masks_sum(maskpath, filename, masks)
    return jsonify({'segmentation_masks': masks_dict,  'segmentation_boxes' : boxes_dict})

@cache.cached(timeout=10000, key_prefix='segmentation_from_model')
def get_segmentation_from_model(data):
    req_data = [{'in_tensor_name': SEG_MODEL_IMAGE_INPUT, 'in_tensor_dtype': SEG_MODEL_IMAGE_TENSOR_TYPE, 'data': data}]
    prediction = clientSegmentation.predict(req_data, request_timeout=10000)
    return prediction

@cache.cached(timeout=10000, key_prefix='inpainting_from_model')
def get_inpainting_from_model(data, mask):
    req_data = [{'in_tensor_name': INP_MODEL_IMAGE_INPUT, 'in_tensor_dtype': INP_MODEL_IMAGE_TENSOR_TYPE, 'data': data}]
    req_data.append({'in_tensor_name': INP_MODEL_MASK_INPUT, 'in_tensor_dtype': INP_MODEL_MASK_TENSOR_TYPE, 'data': mask})
    prediction = clientInpainting.predict(req_data, request_timeout=10000)
    return prediction

def mask_and_photo_normalization(mask, photo):
    print("Input image dims: ", photo.shape)
    print("Input mask dims: ", mask.shape)
    print(photo.shape)
    photo = transform_image(photo[0], [512, 512, 3])
    photo = np.expand_dims(photo, axis=0)
    photo = photo / 255.0
    filepath = app.config['UPLOAD_FOLDER'] +'pic' + '.png'
    plt.imsave(filepath, photo[0])
    print(photo.shape)
    mask = transform_image(mask[0], [512, 512, 3])
    mask = np.expand_dims(mask, axis=0) 
    mask[mask<20]=0
    mask[mask>=20]=255
    mask = mask / 255
    mask = 1 - mask

    return(mask, photo)


def convert_data(raw_data):
    in_memory_file = io.BytesIO()
    raw_data.save(in_memory_file)
    data = np.fromstring(in_memory_file.getvalue(), dtype=np.uint8)
    color_image_flag = 1
    img = cv2.imdecode(data, color_image_flag)
    img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR) 
    img = np.expand_dims(img, axis=0)
    return img


def create_path_if_its_not_exist(filename):
    maskpath = app.config['UPLOAD_FOLDER'] + filename + "/"
    if not os.path.exists(maskpath):
        os.makedirs(maskpath)
    return maskpath

def scale_masks_to_image_size(tensor_dict, image_width, image_height):
    ## Scale the masks to original image size
    if 'detection_masks' in tensor_dict:
        detection_boxes = tf.squeeze(tensor_dict['detection_boxes'], [0])
        detection_boxes = tf.cast(detection_boxes, tf.float32)
        detection_masks = tf.squeeze(tensor_dict['detection_masks'], [0])
        # Reframe is required to translate mask from box coordinates to image coordinates and fit the image size.
        real_num_detection = tf.cast(tensor_dict['num_detections'], tf.int32)
        detection_boxes = tf.slice(detection_boxes, [0, 0], [real_num_detection, -1])
        detection_masks = tf.slice(detection_masks, [0, 0, 0], [real_num_detection, -1, -1])
        detection_masks_reframed = utils_ops.reframe_box_masks_to_image_masks(detection_masks, detection_boxes, image_width, image_height)
        detection_masks_reframed = tf.cast(tf.greater(detection_masks_reframed, 0.5), tf.uint8)
        # Follow the convention by adding back the batch dimension
        tensor_dict['detection_masks'] = tf.expand_dims(detection_masks_reframed, 0)
        tensor_dict['detection_boxes'] = detection_boxes
    sess = tf.InteractiveSession()
    masks = tensor_dict['detection_masks'].eval()
    boxes = tensor_dict['detection_boxes'].eval()
    print("Resulting boxes' tensors dims: ", boxes.shape)
    print("Resulting masks' tensor dims: ", masks.shape)

    return (masks, boxes)



def get_masks_and_boxes_dicts(maskpath, filename, masks, boxes):
    masks_dict = {}
    boxes_dict = {}
    for (index, mask) in enumerate(masks[0]):
        ## TODO Refactor this step. For client testing purpose only 
        filepath = maskpath +'mask_' + str(index) + '.png'
        fileURL = uploadspath + filename + '/' + 'mask_' + str(index) + '.png'
        plt.imsave(filepath, mask, cmap=cm.gray)

        get_transparent_mask(filepath)

        masks_dict['mask_'+ str(index)] = fileURL 
        boxes_dict['mask_box_' + str(index)] = boxes[index].tolist()
    return (masks_dict, boxes_dict) 

def get_transparent_mask(filepath):
        img = Image.open(filepath)
        img = img.convert("RGBA")
        pixdata = img.load()
        for y in range(img.size[1]):
            for x in range(img.size[0]):
                if pixdata[x, y][0] <= 10 and pixdata[x, y][1] <= 10 and pixdata[x, y][2] <= 10:
                    pixdata[x, y] = (0, 0, 0, 0)
        img.save(filepath, "PNG")   

def get_masks_sum(maskpath, filename, masks):
    mask_sum = np.max(masks[0], axis = 0)
    filepath = maskpath +'mask_sum' + '.png'
    fileURL = uploadspath + filename + '/' + 'mask_sum' + '.png'
    plt.imsave(filepath, mask_sum, cmap=cm.gray)
    get_transparent_mask(filepath)
    return fileURL


def transform_image(img, size):
    if (img.shape[0]!=size[0] or img.shape[1]!=size[1]):
        imshape=img.shape
        if imshape[0] > imshape[1]:
            dif = int((imshape[0]-imshape[1])/2.0)
            
            img=img[dif:imshape[0]-dif,:,:]#np.pad(img, ((0,0),((int(imshape[0]-imshape[1])//2),(int(imshape[0]-imshape[1])//2)),(0,0)), 'constant', constant_values=((0,0),(0,0),(0,0)))
        elif imshape[0] < imshape[1]:
            dif = int((imshape[1]-imshape[0])/2.0)
            
            img=img[:,dif:imshape[1]-dif,:]#img=np.pad(img, (((int(imshape[1]-imshape[0])//2),(int(imshape[1]-imshape[0])//2)),(0,0),(0,0)), 'constant', constant_values=((0,0),(0,0),(0,0)))

        img=scipy.misc.imresize(img, size) 
    return img

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3001)
