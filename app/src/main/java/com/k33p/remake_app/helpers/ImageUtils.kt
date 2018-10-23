package com.k33p.remake_app.helpers

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import com.k33p.remake_app.activities.PhotoEditingActivity
import com.k33p.remake_app.graphics.BitmapShaderMaskedDrawable
import com.k33p.remake_app.graphics.MaskedDrawable
import android.graphics.Bitmap
import android.graphics.drawable.Drawable



// Cutting the object for inpainting
fun deletingMaskFromSource(src: Bitmap?, mask: Bitmap?): Bitmap? {
    var mask = mask
    //mask = createTransparentBitmapFromBitmap(mask, Color.BLACK);
    if (mask != null) {
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        tempCanvas.drawBitmap(src!!, 0f, 0f, paint)
        mask = Bitmap.createBitmap(mask)//заменяем на маске Color.BLACK на прозрачный
        tempCanvas.drawBitmap(mask!!, 0f, 0f, null)
        paint.xfermode = null
        return result
    }
    return null
}

fun addMaskForRecover(src: Bitmap, newItem: Bitmap):Bitmap{
    if(newItem != null){
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        tempCanvas.drawBitmap(src, 0f, 0f, paint)
        tempCanvas.drawBitmap(newItem, 0f, 0f, null)
        paint.xfermode = null
        return result
    }
    return src
}

fun removeMaskForRecover(src: Bitmap, newItem: Bitmap):Bitmap{
    if(newItem != null){
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colored = Bitmap.createBitmap(newItem.width, newItem.height, Bitmap.Config.ARGB_8888)
        val canvasColored = Canvas(colored)
        canvasColored.drawColor(Color.BLACK)
        val paintColored = Paint(Paint.ANTI_ALIAS_FLAG)
        paintColored.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        canvasColored.drawBitmap(colored, 0f, 0f, null)
        canvasColored.drawBitmap(newItem, 0f, 0f, paintColored)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        tempCanvas.drawBitmap(src, 0f, 0f, paint)
        tempCanvas.drawBitmap(colored, 0f, 0f, null)
        paint.xfermode = null
        return result
    }
    return src
}


fun selectingObjectsOnMask(src: Bitmap?, masks: ArrayList<Bitmap>): Bitmap? {
    if (masks != null) {
        var result : Bitmap? = src
        /*val colors = intArrayOf(Color.BLUE, Color.GREEN, Color.CYAN, Color.DKGRAY, Color.MAGENTA,
                Color.GRAY, Color.RED, Color.WHITE, Color.YELLOW, Color.LTGRAY)*/
        for ((index, mask) in masks.withIndex()) {
            result = selectingObjectOnMask(result, mask, Color.GREEN)//colors[index % colors.size])
        }
        return result
    }
    return null
}

// Highlighting the object
fun selectingObjectOnMask(src: Bitmap?, mask: Bitmap?, color: Int): Bitmap? {
    if (mask != null) {
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)


        val colored = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val canvasColored = Canvas(colored)
        canvasColored.drawColor(color)
        val paintColored = Paint(Paint.ANTI_ALIAS_FLAG)
        paintColored.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        canvasColored.drawBitmap(colored, 0f, 0f, null)
        val tempMask = PhotoEditingActivity.createTransparentBitmapFromBitmap(mask, Color.BLACK)
        canvasColored.drawBitmap(tempMask!!, 0f, 0f, paintColored)


        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        tempCanvas.drawBitmap(src!!, 0f, 0f, null)
        tempCanvas.drawBitmap(colored, 0f, 0f, paint)
        paint.xfermode = null
        return result
    }
    return null
}

fun selectingObjectOnMaskShader(src: Bitmap?, mask: Bitmap?, color: Int): Bitmap? {
    if (mask != null) {
        val masked: MaskedDrawable
        masked = BitmapShaderMaskedDrawable.getFactory().createMaskedDrawable()
        masked.setPictureBitmap(src)
        masked.setMaskBitmap(mask)
        masked.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.LIGHTEN)
        masked.alpha = 255
        return drawableToBitmap(masked)
    }
    return src
}


// Get the object which was gotten with the mask
fun getObjectByMask(src: Bitmap?, mask: Bitmap?): Bitmap? {
    var mask = mask
    //mask = createTransparentBitmapFromBitmap(mask, Color.BLACK);
    if (mask != null) {
        val result = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        tempCanvas.drawBitmap(src!!, 0f, 0f, null)
        mask = PhotoEditingActivity.createTransparentBitmapFromBitmap(mask, Color.BLACK)//заменяем на маске Color.BLACK на прозрачный
        tempCanvas.drawBitmap(mask!!, 0f, 0f, paint)
        paint.xfermode = null
        return result
    }
    return null
}

fun drawableToBitmap(drawable: Drawable): Bitmap? {
    var bitmap: Bitmap? = null

    if (drawable is BitmapDrawable) {
        if (drawable.bitmap != null) {
            return drawable.bitmap
        }
    }

    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
    } else {
        bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

