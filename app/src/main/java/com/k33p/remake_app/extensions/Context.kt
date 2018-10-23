package com.k33p.remake_app.extensions

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.k33p.remake_app.R
import com.k33p.remaketensorflowservingclient.helpers.CONFIG_PREFS_KEY

fun Context.getConfigSharedPrefs() = getSharedPreferences(CONFIG_PREFS_KEY, Context.MODE_PRIVATE)

