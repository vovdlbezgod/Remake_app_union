package com.k33p.remake_app.activities
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.example.photoeditor.OnPhotoEditorListener
import com.example.photoeditor.PhotoEditor
import com.example.photoeditor.PhotoEditorView
import com.example.photoeditor.ViewType
import com.k33p.remake_app.R
import com.k33p.remake_app.graphics.PropertiesBSFragment
import com.k33p.remake_app.helpers.*
import com.k33p.remake_app.nnclient.NNClient
import com.k33p.remake_app.nnclient.TensorflowServingClient
import com.k33p.remaketensorflowservingclient.helpers.DEFAULT_IMAGE_NAME
import com.k33p.remaketensorflowservingclient.helpers.MasksContainer
import com.k33p.remaketensorflowservingclient.helpers.RemakeTensorflowClientConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException
import kotlin.collections.ArrayList
import kotlin.reflect.KFunction2
import kotlinx.coroutines.experimental.*
import java.util.stream.Stream


class PhotoEditingActivity : AppCompatActivity(), OnPhotoEditorListener, PropertiesBSFragment.Properties {
    private val TAG = "PhotoEditingActivity"
    //private var takePictureButton: Button? = null
    //private var secondView: ImageView? = null
    private var imageView: PhotoEditorView? = null
    private val file: Uri? = null
    //private val mTxtCurrentTool: TextView? = null
    private var mPhotoEditor: PhotoEditor? = null
    private var localBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var basicBitmap: Bitmap? = null
    private var selectedImagePath: String? = null
    private var selectedMaskPath: String? = null
    //private val createdBitmapFromBrush: Bitmap? = null
    private var mPropertiesBSFragment: PropertiesBSFragment? = null
    private var bottomNavigationView : BottomNavigationView? = null

    private var masksContainer: MasksContainer? = null
    private var config: RemakeTensorflowClientConfig? = null
    private var client: NNClient? = null
    private var maskContainer : ArrayList<maskContain> = arrayListOf()
    private var maskContainerForDeleting : ArrayList<maskContain> = arrayListOf()
    private var localBitmapWithMaskSum : Bitmap? = null
    private var maskForRecover : Bitmap? = null

    private val isSDCARDMounted: Boolean
        get() {
            val status = Environment.getExternalStorageState()
            return status == Environment.MEDIA_MOUNTED
        }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_photoediting)
        selectedImagePath = intent.extras!!.getString("selectedImagePath")
        config = RemakeTensorflowClientConfig(this)
        Log.i(TAG, selectedImagePath)
        config!!.imageName =  DEFAULT_IMAGE_NAME
        client = TensorflowServingClient(config!!)

        mPropertiesBSFragment = PropertiesBSFragment()
        mPropertiesBSFragment!!.setPropertiesChangeListener(this)

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val tabListener = BottomNavigationView.OnNavigationItemSelectedListener {
            item -> onNavigationItemSelected(item) }
        bottomNavigationView!!.setOnNavigationItemSelectedListener(tabListener)
        imageView = findViewById<View>(R.id.photoEditorView) as PhotoEditorView
        localBitmap = BitmapFactory.decodeFile(selectedImagePath)
        activityInit()
        val getTouchEvent = View.OnTouchListener { v: View, event: MotionEvent ->
            changeOnTouch(v, event) }
        imageView!!.setOnTouchListener(getTouchEvent)
        /*createdBitmapFromBrush = Bitmap.createBitmap(imageView.getSource().getWidth(), imageView.getSource().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvasColored = new Canvas(createdBitmapFromBrush);
        canvasColored.drawColor(Color.BLACK);*/
         /*try {
            secondView!!.setImageBitmap(maskBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/
        try {
            imageView!!.source.setImageBitmap(localBitmap)
            imageView!!.isDrawingCacheEnabled = true
            basicBitmap = Bitmap.createBitmap(imageView!!.drawingCache)
            imageView!!.isDrawingCacheEnabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mPhotoEditor = PhotoEditor.Builder(this, imageView!!)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                .build() // build photo editor sdk

        mPhotoEditor!!.setOnPhotoEditorListener(this)
        //returnBackWithSavedImage();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //takePictureButton!!.isEnabled = false
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
         when (item.itemId) {
             //R.id.button_image -> null
             R.id.buttonRecover -> {
                 //val mFile = Environment.getExternalStorageDirectory()
                 mPhotoEditor!!.setBrushDrawingMode(false)
                 imageView!!.isDrawingCacheEnabled = true
                 maskForRecover = addMaskForRecover(maskForRecover!!, imageView!!.getmBrushDrawingView().maskBrushing(maskForRecover, imageView!!.drawingCache))
                 imageView!!.isDrawingCacheEnabled = false
                 returnBackWithSavedMask()
                 returnBackWithSavedImage()
                 val imagePath = File(selectedImagePath).absolutePath
                 val maskPath = File(selectedMaskPath).absolutePath
                 //val path = File(mFile, "photo.jpg").absolutePath
                 //val pathM = File(mFile, "mask.jpg").absolutePath
                 imageView!!.getmBrushDrawingView().clearAll()
                 getImageInpainting(this, imagePath, maskPath)
                 //getImageSegmentation(this, path)
             }
             R.id.buttonBrush -> onBrushButtonPressed()
             R.id.buttonReturn -> returnHome()
         }
         return true
    }

    private fun returnHome(){
        val intent = Intent(this, Main2Activity::class.java)
        startActivity(intent)
    }

    private fun onBrushButtonPressed() {
        try {
            mPhotoEditor!!.setBrushDrawingMode(true)
            mPropertiesBSFragment!!.show(supportFragmentManager, mPropertiesBSFragment!!.tag)
            onColorChanged(Color.WHITE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }


    fun activityInit() {
        try {
            val selectedImagePath = intent.extras!!.getString("selectedImagePath")
            //imageView!!.source.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath))
                //Log.d(TAG, "Mask : Height=" + maskBitmap!!.height + " Width=" + maskBitmap!!.width)
                Log.e(TAG, "Image : Height=" + localBitmap!!.height + " Width=" + localBitmap!!.width)
            getImageSegmentation(this, selectedImagePath)
            //maskBitmap = mas
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
    }

    fun activityTestInit(activity: Activity) {
        Thread {
            try {
                activity.runOnUiThread {
                    maskBitmap = BitmapFactory.decodeResource(
                            applicationContext.resources,
                            R.drawable.mask_0)
                    Log.d(TAG, "Mask : Height=" + maskBitmap!!.height + " Width=" + maskBitmap!!.width)
                    Log.d(TAG, "Image : Height=" + localBitmap!!.height + " Width=" + localBitmap!!.width)
                }
                masksContainer = dummyParseBitmapsAndBoxes(this)
                //masks = dummyInitMaskArray(this)
                //drawBitmapWithAllMasks(this, localBitmap, masks, ::selectingObjectsOnMask)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }.start()
    }

    fun drawBitmap(activity: Activity, src: Bitmap?, mask: Bitmap?,
                   func: (Bitmap?, Bitmap?) -> Bitmap?) {
        Thread {
            try {
                val image = func(src, mask)
                activity.runOnUiThread {
                    imageView!!.source.setImageBitmap(image)
                }

            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }.start()
    }

    fun drawBitmapSelect(activity: Activity, src: Bitmap?, mask: ArrayList<maskContain>?, recoverMask: Bitmap?) {
        Thread {
            try {
                val image = selectAllDeletedMasks(src, mask!!, recoverMask)
                activity.runOnUiThread {
                    imageView!!.source.setImageBitmap(image)
                }
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }.start()
    }

    fun getImageInpainting(activity: Activity, imagePath: String, maskPath: String?) {
        Thread {
            if (maskPath == null) {
                Log.e(TAG, "Picture not established")
                //val result = client.getPhotoSegmentation(File(imagePath))
                //return result
            } else {
                val bitmap = client!!.getImageInpainting(imagePath, maskPath)
                activity.runOnUiThread {
                    imageView!!.source.setImageBitmap(bitmap)
                    Log.e(TAG, "Picture established")
                }

            }
        }.start()
    }

    fun getImageSegmentation(activity: Activity, imagePath: String) {
        Thread {
            //drawBitmap(activity, localBitmap, bitmap, ::deletingMaskFromSource)
            Log.i(TAG, imagePath)
            activity.runOnUiThread {
                maskContainer = client!!.getSegmentationMasksContainer(imagePath, localBitmap!!)
                localBitmapWithMaskSum = selectingObjectOnMask(localBitmap, maskContainer[0].maskSum, Color.BLUE)
                maskForRecover = Bitmap.createBitmap(localBitmapWithMaskSum!!.width, localBitmapWithMaskSum!!.height, Bitmap.Config.ARGB_8888)
                drawBitmapSelect(this, localBitmapWithMaskSum, maskContainerForDeleting, maskForRecover)
            }

        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun changeOnTouch(v: View, event: MotionEvent): Boolean {
        val index = event.actionIndex

        // TODO find the real picture sizes
        Log.d(TAG, "Input: (${event.getX(index)}, ${event.getY(index)})")
        val x = event.getX(index)
        val y = event.getY(index)
        imageView!!.isDrawingCacheEnabled = true
        val tempBitmap = Bitmap.createBitmap(imageView!!.drawingCache)
        imageView!!.isDrawingCacheEnabled = false
        maskContainer.parallelStream().forEach {
            if(it.containingPoint(x, y, tempBitmap)){
                Log.e("MaskAdd", "element $it x=$x y=$y")
                if(it.maskSum != it.maskFromTensorflow){
                    if (maskContainerForDeleting.contains(it)) {
                        maskForRecover = removeMaskForRecover(maskForRecover!!, it.maskFromTensorflow)
                        maskContainerForDeleting.remove(it)
                        Log.e("MaskAdd", "Remove $it")
                        return@forEach
                    }else if (!maskContainerForDeleting.contains(it)) {
                        maskForRecover = addMaskForRecover(maskForRecover!!, it.maskFromTensorflow)
                        maskContainerForDeleting.add(it)
                        Log.e("MaskAdd", "Add $it")
                        return@forEach
                    }
                }
            }
        }
        drawBitmapSelect(this, localBitmapWithMaskSum, maskContainerForDeleting, maskForRecover)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //takePictureButton!!.isEnabled = true
            }
        }
    }

    fun selectAllDeletedMasks(src: Bitmap?, masksCont: ArrayList<maskContain>, maskForRec : Bitmap?): Bitmap? {
        if(masksCont.size > 0){
            var result : Bitmap
            result = src!!
            result = selectingObjectOnMask(result, maskForRec, Color.RED)!!
            return result
        }
        return src
    }

    fun takePicture(view: View) {
        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = Uri.fromFile(getOutputMediaFile());
        Toast.makeText(this, "Saved at: " + file.getPath().toString(), Toast.LENGTH_LONG).show();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);

        startActivityForResult(intent, 100);*/

        try {
            mPhotoEditor!!.setBrushDrawingMode(false)
            val tempSrc = Bitmap.createBitmap(basicBitmap!!)
            //Bitmap tempDst = Bitmap.createScaledBitmap(imageView.getDrawingCache(), tempSrc.getWidth(), tempSrc.getHeight(), false);
            imageView!!.isDrawingCacheEnabled = true
            val tempDst = Bitmap.createBitmap(imageView!!.drawingCache)
            imageView!!.isDrawingCacheEnabled = false
            val result = Bitmap.createBitmap(tempSrc.width, tempSrc.height, Bitmap.Config.RGB_565)

            /*Log.d("Size Dst",tempDst.getWidth() +" "+tempDst.getHeight());
            Log.d("Size Src", tempSrc.getWidth()+" "+tempSrc.getHeight());
            Canvas tempCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
            tempCanvas.drawBitmap(tempSrc, 0, 0, null);
            //mask = createTransparentBitmapFromBitmap(mask, Color.BLACK);//заменяем на маске Color.BLACK на прозрачный
            tempCanvas.drawBitmap(tempDst, 0, 0, paint);
            paint.setXfermode(null);*/
            //secondView!!.setImageBitmap(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        returnBackWithSavedImage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                imageView!!.source.setImageURI(file)
            }
        }
    }

    fun returnBackWithSavedMask() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageName = "IMG_$timeStamp.jpg"

        val returnIntent = Intent()
        returnIntent.putExtra("imagePath", saveMask("CameraDemo/Temp", imageName))
        Log.d(TAG, "setResult")
        setResult(Activity.RESULT_OK, returnIntent)
        Log.d(TAG, "after setResult")
    }

    fun returnBackWithSavedImage() {
        object : CountDownTimer(1000, 500) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imageName = "IMG_$timeStamp.jpg"

                val returnIntent = Intent()
                returnIntent.putExtra("imagePath", saveImage("CameraDemo", imageName))
                Log.d(TAG, "setResult")
                setResult(Activity.RESULT_OK, returnIntent)
                Log.d(TAG, "after setResult")
                //finish();
            }
        }.start()
    }

    fun saveImage(folderName: String, imageName: String): String {
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
                if (imageView != null) {
                    imageView!!.isDrawingCacheEnabled = true
                    imageView!!.drawingCache.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //TODO
        selectedImagePath = selectedMaskPath
        return selectedOutputPath
    }

    fun saveMask(folderName: String, imageName: String): String {
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
                maskForRecover!!.compress(Bitmap.CompressFormat.PNG, 80, out)
                out.flush()
                out.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        //TODO
        selectedMaskPath = selectedOutputPath
        return selectedOutputPath
    }

    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
        /*TextEditorDialogFragment textEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode);
        textEditorDialogFragment.setOnTextEditorListener(new TextEditorDialogFragment.TextEditor() {
            @Override
            public void onDone(String inputText, int colorCode) {
                mPhotoEditor.editText(rootView, inputText, colorCode);
                mTxtCurrentTool.setText(R.string.label_text);
            }
        });*/
    }

    override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
        Log.d(TAG, "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onRemoveViewListener(numberOfAddedViews: Int) {
        Log.d(TAG, "onRemoveViewListener() called with: numberOfAddedViews = [$numberOfAddedViews]")
    }

    override fun onStartViewChangeListener(viewType: ViewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
    }



    companion object {
        private val TAG = PhotoEditingActivity::class.java.simpleName


        //заменяет выбранный цвет на прозрачный
        fun createTransparentBitmapFromBitmap(bitmap: Bitmap?, replaceThisColor: Int): Bitmap? {
            if (bitmap != null) {
                val picw = bitmap.width
                val pich = bitmap.height
                val pix = IntArray(picw * pich)
                bitmap.getPixels(pix, 0, picw, 0, 0, picw, pich)

                for (y in pich - 1 downTo 0) {
                    // from left to right
                    for (x in 0 until picw) {
                        val index = y * picw + x
                        val r = pix[index] shr 16 and 0xff
                        val g = pix[index] shr 8 and 0xff
                        val b = pix[index] and 0xff

                        if (pix[index] == replaceThisColor) {
                            pix[index] = Color.TRANSPARENT
                        } else {
                            break
                        }
                    }

                    // from right to left
                    for (x in picw - 1 downTo 0) {
                        val index = y * picw + x
                        val r = pix[index] shr 16 and 0xff
                        val g = pix[index] shr 8 and 0xff
                        val b = pix[index] and 0xff

                        if (pix[index] == replaceThisColor) {
                            pix[index] = Color.TRANSPARENT
                        } else {
                            break
                        }
                    }
                }

                return Bitmap.createBitmap(pix, picw, pich,
                        Bitmap.Config.ARGB_4444)
            }
            return null
        }

        private val outputMediaFile: File?
            get() {
                val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "CameraDemo")

                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d(TAG, "failed to create directory")
                        return null
                    }
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                return File(mediaStorageDir.path + File.separator +
                        "IMG_" + timeStamp + ".jpg")
            }

        fun drawableToBitmap(drawable: Drawable): Bitmap {
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

            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
