package com.k33p.remake_app.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.k33p.remake_app.R
import android.content.Intent



class Main2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun openChangeClose(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("Choice", 1)
        startActivity(intent)
    }

    fun openInpaint(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("Choice", 2)
        startActivity(intent)
    }
}
