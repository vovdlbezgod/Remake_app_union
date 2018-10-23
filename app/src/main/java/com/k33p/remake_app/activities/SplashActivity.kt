package com.k33p.remake_app.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import com.k33p.remake_app.R
import com.k33p.remaketensorflowservingclient.helpers.SPLASH_TIME_OUT


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)
        Handler().postDelayed(Runnable {
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))


            finish()
        }, SPLASH_TIME_OUT)
    }
}