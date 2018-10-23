package com.k33p.remake_app

import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.util.Log

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File
import android.graphics.Bitmap
import android.R.attr.bitmap
import android.graphics.Point
import com.k33p.remake_app.net.OkhttpRemakeTensorflowServingHTTPClient
import com.k33p.remake_app.net.RemakeTensorflowServingHTTPClient
import com.k33p.remaketensorflowservingclient.helpers.*
import java.io.FileOutputStream
import java.io.IOException


// TODO write test
@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class  RemakeTensorflowServingClientTest {
    private val TAG = "UploaderTest"

    // Config for passing Roboelectric + Okhttp issue
    @Config(sdk = [23])
    @Test
    fun photoUploader_isCorrect() {
        val context = RuntimeEnvironment.application
        val config = RemakeTensorflowClientConfig(context)
        val client = OkhttpRemakeTensorflowServingHTTPClient(config)

        val photo = File("/home/k33p/Downloads/255.jpg")
        val result: String? = client.getPhotoSegmentationJSON(photo)
        //println("Result: $TAG: $result")
    }

    @Config(sdk = [23])
    @Test
    fun segmenatation_isCorrect() {
        // TODO make it more normal :)
        val context = RuntimeEnvironment.application
        val config = RemakeTensorflowClientConfig(context)
        //val server = MockWebServer()

        // Schedule some responses.
        //server.enqueue(MockResponse().setBody("hello, world!"))


        // Start the server.
        //server.start()
        //val baseUrl = server.url(config.serverSegmentationPath)
        //Log.i(TAG, "Test server URL: $baseUrl")
        //config.serverEndpoint = baseUrl.toString().substringBefore(config.serverSegmentationPath)

        val photo = File("/home/k33p/Downloads/255.jpg")
        val client = OkhttpRemakeTensorflowServingHTTPClient(config)
        //println("Test server URL: ${config.serverEndpoint}${config.serverSegmentationPath}")
        val result: String? = client.getPhotoSegmentationJSON(photo)

        //val masksContainer = BoxesMasksContainer()
        //masksContainer.parseMasksFromString(result!!, client)
        //println("Hello: ${boxesAndBitmapsContainer.bitmapContainingPoint(Pair(0.5f, 0.3f))}")
        //val box = Box()

        //box.parseFromString(boxesArray[2])
        //println("Size: ${bitmapsArray[14].byteCount}")

        //val dir = File("/home/k33p/Downloads/photos")
        //if(!dir.exists())dir.mkdirs()
        //val file = File("/home/k33p/Downloads/photos", "mask_1.png")

          //  val fOut = FileOutputStream(file)
          //  val bitmap = bitmapArray[1]
          //  bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)


            //    fOut.close()


}
    @Config(sdk = [23])
    @Test
    fun inpainting_isCorrect() {
        // TODO make it more normal :)
        val context = RuntimeEnvironment.application
        val config = RemakeTensorflowClientConfig(context)

        val photo = File("/home/k33p/Downloads/photo.jpg")
        val mask = File("/home/k33p/Downloads/mask.jpg")
        val client = OkhttpRemakeTensorflowServingHTTPClient(config)
        //println("Test server URL: ${config.serverEndpoint}${config.serverSegmentationPath}")
        val result = client.getPhotoInpainting(photo, mask)
        //println(result)
        //val boxesAndBitmapsContainer = BoxesAndBitmapsContainer()
        //boxesAndBitmapsContainer.parseBitmapsAndBoxesFromString(result!!)
        //println("Hello: ${boxesAndBitmapsContainer.bitmapContainingPoint(Pair(0.5, 0.3))}")

    }
}
