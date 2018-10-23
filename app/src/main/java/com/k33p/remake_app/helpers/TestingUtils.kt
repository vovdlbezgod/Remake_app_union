package com.k33p.remake_app.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.k33p.remake_app.R
import com.k33p.remaketensorflowservingclient.helpers.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList


fun dummyInitMaskArray(context: Context) : ArrayList<Bitmap> {
    val masks : ArrayList<Bitmap> = arrayListOf()
    var j = 0
    for (i in 0..14) {
        j = if (i > 9) i + 81 else i
        val decodedByte = BitmapFactory.decodeResource(context.resources,
                R.drawable.mask_0 + i)
        masks.add(decodedByte)}
    return masks
}

// For testing purpose only
private fun readFile(context: Context, resourceId : Int) : String? {
    try {
        //Load File
        val jsonReader = BufferedReader(InputStreamReader(context.resources.openRawResource(
                resourceId)))
        val jsonBuilder = StringBuilder()
        var line : String? = jsonReader.readLine()

        while (line != null) {
            jsonBuilder.append(line).append("\n")
            line = jsonReader.readLine()
        }
        Log.i(TAG, "JSON parsed file: \n ${jsonBuilder.toString()}")
        return jsonBuilder.toString()
    } catch (e : FileNotFoundException) {
        Log.e(TAG, "json: file not found")
    } catch (e : IOException) {
        Log.e(TAG, "json: ioerror")
    } catch (e : JSONException) {
        Log.e(TAG, "json: error while parsing json")
    }
    return null
}

// For testing purpose only
fun dummyParseBitmapsAndBoxes(context: Context) : MasksContainer {
    val boxesAndBitmapsArray = arrayListOf<Pair<Box, Bitmap>>()
    val strObj = JSONObject(readFile(context, R.raw.masks))
    val boxStr = strObj.get(SEG_KEY_BOXES_LIST)
    val jbox = JSONObject(boxStr.toString())
    var j = 0
    // I changed the enumeration because R constants are in the wrong order
    for (i in 0 until jbox.length()) {
        j = if (i > 9) i + 81
        else i
        val decodedByte = BitmapFactory.decodeResource(context.resources,
                R.drawable.mask_0 + i)
        Log.i(TAG, "Bitmap$j: $decodedByte")
        // Get box coordinates
        val boxItem = jbox.get(SEG_KEY_BOXES_LIST_ITEM + j)
        Log.i(TAG, "Box $i: $boxItem")
        val box = Box()
        box.parseFromString(boxItem.toString())
        boxesAndBitmapsArray.add(Pair(box, decodedByte))
    }
    val maskSum = BitmapFactory.decodeResource(context.resources,
            R.drawable.mask_0 )
    return BoxesMasksContainer(boxesAndBitmapsArray, maskSum)
}

