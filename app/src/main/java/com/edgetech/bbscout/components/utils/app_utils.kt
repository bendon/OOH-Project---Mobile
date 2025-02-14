package com.edgetech.bbscout.components.utils

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.datatransport.BuildConfig


val isDebug: Boolean get() = BuildConfig.DEBUG
fun logD(message: String, e: java.lang.Exception? = null, location: String? = null) = if (isDebug)
    Log.e("${location ?: "Logging"}", "$message${e?.localizedMessage ?: ""}") else null

@SuppressLint("LogNotTimber")
fun Any.log(message: String? = "", e: java.lang.Exception? = null) = if (isDebug)
    Log.e(this.javaClass.simpleName, "$message\n${e?.localizedMessage ?: ""}") else null