package com.k33p.remaketensorflowservingclient.helpers

// Splash screen delay
const val SPLASH_TIME_OUT = 100L
// Image information
const val DEFAULT_IMAGE_NAME = "testImage.png"
// TODO Hardcoded, needs to be generated
const val DEFAULT_IMAGE_NAME_KEY = "data"

const val DEFAULT_MASK_NAME = "testMask.png"
// TODO Hardcoded, needs to be generated
const val DEFAULT_MASK_NAME_KEY = "mask"

// User Server Information
// TODO Hardcoded, needs to be generated
const val DEFAULT_USER_ID = "8457851245"
const val DEFAULT_USER_ID_KEY = "userid"

// Tensorflow Serving connection
const val DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0"
const val DEFAULT_PASSWORD = "my-secret"
const val DEFAULT_USER_NAME = "token"
const val DEFAULT_SERVER_ENDPOINT = "http://192.168.137.1:3001"
const val DEFAULT_SERVER_SEGMENTATION_PATH = "/segmentation"
const val DEFAULT_SERVER_INPAINTING_PATH = "/inpainting"
const val DEFAULT_CONNECT_TIMEOUT = 100000
const val DEFAULT_READ_TIMEOUT = 300000
const val DEFAULT_TIMESTAMP = "0"


// Configuration Shared Preferences
const val CONFIG_PREFS_KEY = "com.k33p.remaketensorflowclient.config"
const val USER_AGENT = "user_agent"
const val PASSWORD = "auth_password"
const val USER_NAME = "user_name"
const val SERVER_ENDPOINT = "server_endpoint"
const val CONNECT_TIMEOUT = "connect_timeout"
const val READ_TIMEOUT = "read_timeout"
const val TIMESTAMP = "timestamp"
const val SERVER_SEGMENTATION_PATH = "seg_server_path"
const val SERVER_INPAINTING_PATH = "inp_server_path"
const val IMAGE_NAME = "image_name"
const val IMAGE_NAME_KEY = "image_name_key"
const val MASK_NAME = "mask_name"
const val MASK_NAME_KEY = "mask_name_key"
const val USER_ID = "user_id"
const val USER_ID_KEY = "user_id_key"


// JSON segmentation match keys
const val SEG_KEY_MASKS_LIST = "segmentation_masks"
const val SEG_KEY_BOXES_LIST = "segmentation_boxes"
const val SEG_KEY_MASKS_LIST_ITEM = "mask_"
const val SEG_KEY_BOXES_LIST_ITEM = "mask_box_"
const val SEG_KEY_MASKS_SUM = "mask_sum"
