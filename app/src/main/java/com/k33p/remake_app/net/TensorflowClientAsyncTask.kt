package com.k33p.remake_app.net

import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.k33p.remaketensorflowservingclient.helpers.RemakeTensorflowClientConfig
import java.io.File


class DownloadSegmentationJSONTask(val config: RemakeTensorflowClientConfig)
    : AsyncTask<String, Void, String?>() {
    private val TAG = "downloadSegmJSONTask"
    private val client = config.client

    override fun doInBackground(vararg params: String): String? {
        return try {
            val imagePath = params[0]
            client.getPhotoSegmentationJSON(File(imagePath))
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

}

//TODO maybe add array downloading, needs to watch some optimizations articles
class DownloadMasksTask(val config: RemakeTensorflowClientConfig)
    : AsyncTask<ArrayList<String>, Void, ArrayList<Bitmap?>?>() {
    private val TAG = "downloadMaskTask"
    private val client = config.client
    private val bitmapList = arrayListOf<Bitmap?>()
    override fun doInBackground(vararg params: ArrayList<String>): ArrayList<Bitmap?>? {
        return try {
            val imageURLList = params[0]
            for (imageURL in imageURLList) bitmapList.add(client.getPhotoByURL(imageURL))
            bitmapList
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

}

class DownloadInpaintingTask(val config: RemakeTensorflowClientConfig)
    : AsyncTask<String, Void, Bitmap?>() {
    private val TAG = "downloadInpaintingTask"
    private val client = config.client
    private var bitmap: Bitmap? = null

    override fun doInBackground(vararg params: String): Bitmap? {
        return try {
            val imagePath = params[0]
            val maskPath = params[1]
            bitmap = client.getPhotoInpainting(File(imagePath), File(maskPath))
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

}
