package com.k33p.remake_app.activities

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.k33p.remake_app.R


class ChoiceActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var bottomNavigationView : BottomNavigationView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)

        imageView = findViewById<View>(R.id.imageview) as ImageView
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val tabListener = BottomNavigationView.OnNavigationItemSelectedListener {
            item: MenuItem ->
            when(item.itemId) {
                R.id.buttonRecover -> null
            }
            false

        }
        bottomNavigationView!!.setOnNavigationItemSelectedListener(tabListener)

    }
}