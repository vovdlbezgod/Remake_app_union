package com.k33p.remake_app.net

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.k33p.remaketensorflowservingclient.helpers.RemakeTensorflowClientConfig
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit


interface RemakeTensorflowServingHTTPClient {
    fun getPhotoSegmentationJSON(photo: File): String?
    fun getPhotoInpainting(photo: File, mask: File): Bitmap?
    fun getPhotoByURL(path: String) : Bitmap?
    fun getChangingClothes(photo: File, cloth: File): Bitmap?
}

// TODO rewrite this staff
class OkhttpRemakeTensorflowServingHTTPClient(val config: RemakeTensorflowClientConfig)
    : RemakeTensorflowServingHTTPClient {
    override fun getChangingClothes(photo: File, cloth: File): Bitmap? {
        try {
            val reqBody: RequestBody = buildRequestBody(photo, cloth)
            val request = buildPostRequest(config.serverEndpoint, config.serverWardrobePath,
                    reqBody)
            val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            client.connectTimeoutMillis()
            val response = client.newCall(request).execute()
            Log.i("Response", "uploadImage:" + response.body()!!.source().toString())

            val inputStream = response.body()!!.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return bitmap

        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.toString())
            return null
        }    }

    // TODO refactor this hack after server refactoring (adding cache for each user)

    override fun getPhotoInpainting(photo: File, mask: File): Bitmap? {
        try {
            val reqBody: RequestBody = buildRequestBody(photo, mask)
            val request = buildPostRequest(config.serverEndpoint, config.serverInpaintingPath,
                    reqBody)
            val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            client.connectTimeoutMillis()
            val response = client.newCall(request).execute()
            Log.i("Response", "uploadImage:" + response.body()!!.source().toString())

            val inputStream = response.body()!!.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return bitmap

        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.toString())
            return null
        }
    }

    override fun getPhotoSegmentationJSON(photo: File): String? {
        try {
            val reqBody: RequestBody = buildRequestBody(photo)
            val request = buildPostRequest(config.serverEndpoint, config.serverSegmentationPath,
                                      reqBody)
            val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            client.connectTimeoutMillis()
            val response = client.newCall(request).execute()
            Log.i("Response", "uploadImage:"+ response.body()!!.source().toString())

            return response.body()!!.string()
        } catch (e : Exception) {
            Log.e(TAG, "Error: " + e.toString())
            return null
        }

    }

    override fun getPhotoByURL(path: String) : Bitmap? {
        try {
            val request = buildGetRequest(config.serverEndpoint, path)
            val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            client.connectTimeoutMillis()
            val response = client.newCall(request).execute()
            Log.i("Response", "uploadImage:" + response.body()!!.source().toString())

            val inputStream = response.body()!!.byteStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error: " + e.localizedMessage)
            return null
        }
    }

    private fun buildPostRequest(serverEndpoint : String, serverPath: String,
                             req: RequestBody) =
            Request.Builder()
            .url("$serverEndpoint$serverPath")
            .post(req)
            .build()

    private fun buildGetRequest(serverEndpoint : String,
                                 path: String) =
            Request.Builder()
                    .url("$serverEndpoint$path")
                    .build()

    private fun buildRequestBody(photo: File) = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(config.userIdKey, config.userId)
            .addFormDataPart(config.imageNameKey, config.imageName,
                    RequestBody.create(MediaType.parse("image/jpg"), photo))
            .build()


    // TODO delete this hack after server refactoring (adding cache for each user)
    private fun buildRequestBody(photo: File, mask: File) = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(config.imageNameKey, config.imageName,
                    RequestBody.create(MediaType.parse("image/jpg"), photo))
            .addFormDataPart(config.maskNameKey, config.maskName,
                    RequestBody.create(MediaType.parse("image/jpg"), mask))
            .addFormDataPart(config.userIdKey, config.userId)
            .build()

}

