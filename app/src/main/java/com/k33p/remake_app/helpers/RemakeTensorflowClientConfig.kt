package com.k33p.remaketensorflowservingclient.helpers

import android.content.Context
import com.k33p.remake_app.extensions.getConfigSharedPrefs
import com.k33p.remake_app.net.OkhttpRemakeTensorflowServingHTTPClient
import com.k33p.remake_app.net.RemakeTensorflowServingHTTPClient

/**
 * A wrapper for SharedPreferences that stores and reads the values for the Client.
 */
// TODO add singleton (maybe object?)
class RemakeTensorflowClientConfig(val context: Context) {
    private val prefs = context.getConfigSharedPrefs()
    val client = OkhttpRemakeTensorflowServingHTTPClient(this)

    // Parameters for Tensorflow Serving server
    var serverSegmentationPath: String
        get() = prefs.getString(SERVER_SEGMENTATION_PATH, DEFAULT_SERVER_SEGMENTATION_PATH)
        set(serverSegmentationPath) = prefs.edit().putString(SERVER_SEGMENTATION_PATH,
                                                serverSegmentationPath).apply()

    var serverInpaintingPath: String
        get() = prefs.getString(SERVER_INPAINTING_PATH, DEFAULT_SERVER_INPAINTING_PATH)
        set(serverSegmentationPath) = prefs.edit().putString(SERVER_INPAINTING_PATH,
                serverSegmentationPath).apply()

    var serverWardrobePath: String
        get() = prefs.getString(SERVER_WARDROBE_PATH, DEFAULT_SERVER_WARDROBE_PATH)
        set(serverSegmentationPath) = prefs.edit().putString(SERVER_WARDROBE_PATH,
                serverSegmentationPath).apply()

    var serverEndpoint: String
        get() = prefs.getString(SERVER_ENDPOINT, DEFAULT_SERVER_ENDPOINT)
        set(serverEndpoint) = prefs.edit().putString(SERVER_ENDPOINT, serverEndpoint).apply()

    var connectTimeout: Int
        get() = prefs.getInt(CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT)
        set(connectTimeout) = prefs.edit().putInt(CONNECT_TIMEOUT, connectTimeout).apply()

    var readTimeout: Int
        get() = prefs.getInt(READ_TIMEOUT, DEFAULT_READ_TIMEOUT)
        set(readTimeout) = prefs.edit().putInt(READ_TIMEOUT, readTimeout).apply()

    var lastTimeStamp: String
        get() = prefs.getString(TIMESTAMP, DEFAULT_TIMESTAMP)
        set(lastTimeStamp) = prefs.edit().putString(TIMESTAMP, lastTimeStamp).apply()

    var userAgent: String
        get() = prefs.getString(USER_AGENT, DEFAULT_USER_AGENT)
        set(userAgent) = prefs.edit().putString(USER_AGENT, userAgent).apply()

    var name: String
        get() = prefs.getString(USER_NAME, DEFAULT_USER_NAME)
        set(name) = prefs.edit().putString(USER_NAME, name).apply()

    var password: String
        get() = prefs.getString(PASSWORD, DEFAULT_PASSWORD)
        set(password) = prefs.edit().putString(PASSWORD, password).apply()

    var imageName: String
        get() = prefs.getString(IMAGE_NAME, DEFAULT_IMAGE_NAME)
        set(imageName) = prefs.edit().putString(IMAGE_NAME, imageName).apply()

    val imageNameKey: String
        get() = prefs.getString(IMAGE_NAME_KEY, DEFAULT_IMAGE_NAME_KEY)

    val maskName: String
        get() = prefs.getString(MASK_NAME, DEFAULT_MASK_NAME)

    val maskNameKey: String
        get() = prefs.getString(MASK_NAME_KEY, DEFAULT_MASK_NAME_KEY)

    val userId: String
        get() = prefs.getString(USER_ID, DEFAULT_USER_ID)

    val userIdKey: String
        get() = prefs.getString(USER_ID_KEY, DEFAULT_USER_ID_KEY)

}