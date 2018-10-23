package com.k33p.remake_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.k33p.remake_app.R;


public class MainActivity extends MediaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openUserGallery(View view) {
        openGallery();
    }

    public void openUserCamera(View view) {
        startCameraActivity();
    }

    @Override
    protected void onPhotoTaken() {
        Intent iin= getIntent();
        int b = iin.getExtras().getInt("Choice");
        if(b == 1){
            Intent intent = new Intent(MainActivity.this, changing_cloth.class);
            intent.putExtra("selectedImagePath", selectedImagePath);
            startActivity(intent);
        }
        if(b == 2){
            Intent intent = new Intent(MainActivity.this, PhotoEditingActivity.class);
            intent.putExtra("selectedImagePath", selectedImagePath);
            startActivity(intent);
        }
    }
}

/*
class MainActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Example of a call to a native method
        //sample_text.text = stringFromJNI()
        }

        /**
         * A native method that is implemented by the 'native-lib' native library,
         * which is packaged with this application.
         */
    /*
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }*/



