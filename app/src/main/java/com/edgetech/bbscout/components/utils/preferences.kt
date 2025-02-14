package com.edgetech.bbscout.components.utils

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.lang.reflect.Type


fun setPreference(
    context: Context,
    key: String,
    value: Any
) {
    val sp = context.applicationContext.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    if (sp != null) {
        sp.edit().putString(key, value.toJson()).apply()
        sp.edit().apply()
    }
}

fun <T> getPreference(context: Context, key: String, t: Class<T>): T? {
    val sp = context.applicationContext.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    return sp!!.getString(key, "").let {
        Log.e("PREF_GET", key)
        if (it == null) {
            null
        } else {
            try {
                var i = Gson().fromJson(it, t)
                i
            } catch (e: Exception) {
                null
            }
        }
    }
}

