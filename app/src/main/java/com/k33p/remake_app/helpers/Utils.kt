package com.k33p.remaketensorflowservingclient.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapRegionDecoder

import android.util.Log
import com.k33p.remake_app.R
import com.k33p.remake_app.net.RemakeTensorflowServingHTTPClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject



const val TAG = "Util tools"
class Box {
    lateinit var firstPoint : Pair<Double, Double>
    lateinit var secondPoint : Pair<Double, Double>

    override fun toString(): String {
        return super.toString() + "\n[first point: (${firstPoint.first}, ${firstPoint.second}) \n" +
                "second point: (${secondPoint.first}, ${secondPoint.second})]"
    }

    // We suppose that the box is always correctly parsed
    fun pointIsInBox(otherPoint : Pair<Float, Float>) : Boolean {
        if (firstPoint.first >= secondPoint.first && firstPoint.second >= secondPoint.second)
            Log.e(TAG, "This box's points are in the wrong order \n" +
                    "first x: ${firstPoint.first}, first y: ${firstPoint.second} \n" +
                    "second x: ${secondPoint.first}, second y: ${secondPoint.second}")
        return (otherPoint.first in firstPoint.first..secondPoint.first) &&
                (otherPoint.second in firstPoint.second..secondPoint.second)
    }

    fun getMetric() : Double {
        return (firstPoint.first - secondPoint.first) * (firstPoint.second - secondPoint.second)
    }

    fun parseFromString(str : String) {
        val arr = JSONArray(str)
        firstPoint = Pair(arr.getDouble(1), arr.getDouble(0))
        secondPoint = Pair(arr.getDouble(3), arr.getDouble(2))
        Log.i(TAG, "from $firstPoint to $secondPoint parsed")
    }
}

interface MasksContainer {
    val maskSum: Bitmap
    fun maskContainingPoint(point: Pair<Float, Float>) : Bitmap?
    fun maskWithIndexContainingPoint(point: Pair<Float, Float>) : Pair<Int, Bitmap?>
}
class BoxesMasksContainer(private val boxesAndBitmapsArray: ArrayList<Pair<Box, Bitmap>>,
                          override val maskSum: Bitmap) : MasksContainer {
    val size
        get() = boxesAndBitmapsArray.size

    fun add(bitmap: Bitmap, box : Box) {
        boxesAndBitmapsArray.add(Pair(box, bitmap))
    }

    override fun maskContainingPoint(point: Pair<Float, Float>) : Bitmap? {
        var minBitmap : Bitmap? = null
        var minMetric = Double.MAX_VALUE
        Log.d(TAG, "Input coordinate: $point")
        for ((index, pair) in boxesAndBitmapsArray.withIndex()) {
            if (pair.first.pointIsInBox(point) && pair.first.getMetric() < minMetric) {
                Log.d(TAG, "Box$index: ${pair.first}")
                minBitmap = pair.second
                minMetric = pair.first.getMetric()
            }
        }
        return minBitmap
    }

    override fun maskWithIndexContainingPoint(point: Pair<Float, Float>) : Pair<Int, Bitmap?> {
        var minBitmap : Bitmap? = null
        var minMetric = Double.MAX_VALUE
        var minIndex = -1
        Log.d(TAG, "Input coordinate: $point")
        for ((index, pair) in boxesAndBitmapsArray.withIndex()) {
            if (pair.first.pointIsInBox(point) && pair.first.getMetric() < minMetric) {
                Log.d(TAG, "Box$index: ${pair.first}")
                minBitmap = pair.second
                minMetric = pair.first.getMetric()
                minIndex = index
            }
        }
        return Pair(minIndex, minBitmap)
    }


    //TODO delete client, add masksSum
    fun parseMasksFromJSONString(result : String, client: RemakeTensorflowServingHTTPClient) {
        val strObj = JSONObject(result)
        val maskStr = strObj.get(SEG_KEY_MASKS_LIST)
        val boxStr = strObj.get(SEG_KEY_BOXES_LIST)
        Log.i(TAG, "Res: $maskStr")
        val jmask = JSONObject(maskStr.toString())
        val jbox = JSONObject(boxStr.toString())
        for (i in 0 until jbox.length()) {
            // Get mask bitmap
            val maskItem = jmask.get(SEG_KEY_MASKS_LIST_ITEM + i)
            Log.i(TAG, "Mask $i: $maskItem")
            //val decodedString = Base64.decode(maskItem.toString(), Base64.DEFAULT)
            //val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            val decodedByte = client.getPhotoByURL(maskItem.toString())
            // Get box coordinates
            val boxItem = jbox.get(SEG_KEY_BOXES_LIST_ITEM + i)
            //println("Box $i: $boxItem")
            Log.i(TAG, "Box $i: $boxItem")
            val box = Box()
            box.parseFromString(boxItem.toString())
            this.add(decodedByte!!, box)
        }
        Log.i(TAG, "Lengths: bitmaps and boxes: ${this.size}")
    }

}

