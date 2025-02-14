package com.edgetech.bbscout.components.utils

import android.content.Context
import com.google.gson.Gson
import java.lang.reflect.Type

fun Any.gson() = Gson().toJson(this)
fun Any.toJson(): String = Gson().toJson(this)



fun <T> Any.fromJson(t: Type): T? {

    return try {
        gson().let { Gson().fromJson<T>(it, t) }
    } catch (e: Exception) {
        log("An error $e when trying to deserialise $this")
        null
    }

}