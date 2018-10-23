package com.k33p.remake_app.nnclient

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.k33p.remake_app.R
import com.k33p.remake_app.helpers.getObjectByMask
import com.k33p.remake_app.helpers.maskContain
import com.k33p.remake_app.net.*
import com.k33p.remaketensorflowservingclient.helpers.*
import org.json.JSONObject
import java.io.File


interface NNClient {
    fun getSegmentationMasksContainer(imagePath: String, src: Bitmap): ArrayList<maskContain>
    fun getImageInpainting(imagePath: String, maskPath:String): Bitmap?
}

class TensorflowServingClient(val config: RemakeTensorflowClientConfig) : NNClient {
    val downloadSegmentationJSONTask = DownloadSegmentationJSONTask(config)
    val downloadInpaintingTask = DownloadInpaintingTask(config)
    val downloadMasksTask = DownloadMasksTask(config)

    override fun getImageInpainting(imagePath: String, maskPath: String): Bitmap? {
        return downloadInpaintingTask.execute(imagePath, maskPath).get()
    }

    override fun getSegmentationMasksContainer(imagePath: String, src: Bitmap): ArrayList<maskContain> {
        val jsonString = downloadSegmentationJSONTask.execute(imagePath).get()
        val boxesAndBitmapsArray = arrayListOf<Pair<Box, Bitmap>>()
        val (parsedURLList, boxesList) = parseSegmentationJSON(jsonString!!)
        val bitmapList = downloadMasksTask.execute(parsedURLList).get()
        for (idx in 0 until boxesList.size) {
            boxesAndBitmapsArray.add(Pair(boxesList[idx], bitmapList!![idx]!!))
        }
        val maskSumBitmap = bitmapList!![bitmapList.size - 1]
        val masks : ArrayList<maskContain> = arrayListOf()
        for (i in bitmapList){
            val maskMem : maskContain = maskContain(i, getObjectByMask(src, i), maskSumBitmap)
            masks.add(maskMem)
        }
        return masks
    }

    private fun parseSegmentationJSON(jsonString : String) : Pair<ArrayList<String>, ArrayList<Box>> {
        val parsedURLList = arrayListOf<String>()
        val boxList = arrayListOf<Box>()
        val strObj = JSONObject(jsonString)
        val maskStr = strObj.get(SEG_KEY_MASKS_LIST)
        val boxStr = strObj.get(SEG_KEY_BOXES_LIST)
        val jmask = JSONObject(maskStr.toString())
        val jbox = JSONObject(boxStr.toString())

        Log.i(TAG, "Res: $maskStr")
        for (i in 0 until jbox.length()) {
            // Get mask bitmap
            val maskItemURL = jmask.get(SEG_KEY_MASKS_LIST_ITEM + i).toString()
            parsedURLList.add(maskItemURL)
            // Get box coordinates
            val boxItem = jbox.get(SEG_KEY_BOXES_LIST_ITEM + i)
            val box = Box()
            box.parseFromString(boxItem.toString())
            boxList.add(box)
        }
        val maskSumURL = jmask.get(SEG_KEY_MASKS_SUM).toString()
        parsedURLList.add(maskSumURL)
        return Pair(parsedURLList, boxList)
    }

}