package com.k33p.remake_app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.example.photoeditor.PhotoEditor
import com.example.photoeditor.PhotoEditorView
import com.example.photoeditor.SaveSettings
import com.k33p.remake_app.R
import com.k33p.remake_app.R.drawable.aa
import com.k33p.remake_app.activities.StickerBSFragment
import com.k33p.remake_app.net.OkhttpRemakeTensorflowServingHTTPClient
import com.k33p.remake_app.nnclient.NNClient
import com.k33p.remaketensorflowservingclient.helpers.RemakeTensorflowClientConfig
import kotlinx.android.synthetic.main.activity_changing_cloth.*
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutionException
import org.jetbrains.anko.toast
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class changing_cloth : MediaActivity(), StickerBSFragment.StickerListener{
    override fun onPhotoTaken() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStickerClick(bitmap: Int) {
        /*var res = when(bitmap){
            2131230815 -> BitmapFactory.decodeResource(resources, R.drawable.res_b)
            2131230816 -> BitmapFactory.decodeResource(resources, R.drawable.res_c)
            2131230817 -> BitmapFactory.decodeResource(resources, R.drawable.res_d)
            2131230821 -> BitmapFactory.decodeResource(resources, R.drawable.res_f)
            else -> localBitmap
        }*/
        //drawBitmap(this, res)
        try{
            choosenClothes = BitmapFactory.decodeResource(resources, bitmap)
            showLoading("Changing your T-Shirt")
            returnBackWithSavedClothes()
            getImageInpainting(this, selectedImagePathClothChangActivity.toString(), choosenClothesPath)
            hideLoading()
        }
        catch (e:Exception){
            Log.e("getChanginClothe", e.message)
        }
    }

    private var selectedImagePathClothChangActivity: String? = null
    private val TAG = "Changing_cloth"
    private var localBitmap: Bitmap? = null
    private var imageView: PhotoEditorView? = null
    private var mPhotoEditor: PhotoEditor? = null
    private var mStickerBSFragment: StickerBSFragment? = null
    private var config: RemakeTensorflowClientConfig? = null
    private var client: OkhttpRemakeTensorflowServingHTTPClient? = null
    private var choosenClothes: Bitmap? = null
    private var choosenClothesPath: String? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_change -> {
                mStickerBSFragment!!.show(supportFragmentManager, mStickerBSFragment!!.getTag())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_save -> {
                saveImage()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        makeFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changing_cloth)
        mStickerBSFragment = StickerBSFragment()
        mStickerBSFragment!!.setStickerListener(this)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        selectedImagePathClothChangActivity = intent.extras!!.getString("selectedImagePath")
        activityInit()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //takePictureButton!!.isEnabled = false
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    fun activityInit() {
        try {
            config = RemakeTensorflowClientConfig(this)
            client = config!!.client
            imageView = findViewById<View>(R.id.imageShow) as PhotoEditorView
            val selectedImagePath = intent.extras!!.getString("selectedImagePath")
            getImage(this, selectedImagePath)
            //maskBitmap = mas
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }

    fun getImage(activity: Activity, imagePath: String) {
        Thread {
            Log.i(TAG, imagePath)
            activity.runOnUiThread {
                localBitmap = BitmapFactory.decodeFile(imagePath)
                drawBitmap(this, localBitmap)
            }

        }.start()
    }

    fun drawBitmap(activity: Activity, src: Bitmap?) {
        Thread {
            try {
                activity.runOnUiThread {
                    mPhotoEditor = PhotoEditor.Builder(this, imageView).build() // build photo editor sdk
                    imageView!!.source.setImageBitmap(src)
                }

            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }.start()
    }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo")
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Failed to create directory")
                }
            }
            val file = File(mediaStorageDir.path + File.separator + ""
                    + System.currentTimeMillis() + ".jpg")
            try {
                file.createNewFile()

                val saveSettings = SaveSettings.Builder()
                        .setClearViewsEnabled(true)
                        .setTransparencyEnabled(true)
                        .build()

                mPhotoEditor!!.saveAsFile(file.absolutePath, saveSettings, object : PhotoEditor.OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        hideLoading()
                        showSnackbar("Image Saved Successfully in $imagePath")
                        imageView!!.source.setImageURI(Uri.fromFile(File(imagePath)))
                    }

                    override fun onFailure(exception: Exception) {
                        hideLoading()
                        showSnackbar("Failed to save Image")
                    }
                })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                showSnackbar(e.message!!)
            }

        }
    }

    fun getImageInpainting(activity: Activity, imagePath: String, maskPath: String?) {
        Thread {
            val bitmap = client!!.getChangingClothes(File(imagePath), File(maskPath))
            activity.runOnUiThread {
                imageView!!.source.setImageBitmap(bitmap)
                Log.e(TAG, "Picture established")
            }
        }.start()
    }

    fun returnBackWithSavedClothes() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = "IMG_$timeStamp.jpg"

        val returnIntent = Intent()
        returnIntent.putExtra("imagePath", saveChoosen("CameraDemo/Temp", imageName))
        Log.d(TAG, "setResult")
        setResult(Activity.RESULT_OK, returnIntent)
        Log.d(TAG, "after setResult")
    }

    fun saveChoosen(folderName: String, imageName: String): String {
        var selectedOutputPath = ""
        if (isSDCARDMounted) {
            val mediaStorageDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Failed to create directory")
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.path + File.separator + imageName
            Log.d(TAG, "selected camera path $selectedOutputPath")
            val file = File(selectedOutputPath)
            try {
                val out = FileOutputStream(file)
                choosenClothes!!.compress(Bitmap.CompressFormat.PNG, 80, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //TODO
        choosenClothesPath = selectedOutputPath
        return selectedOutputPath
    }

}
