package com.dev_vlad.car_v.util

import android.util.Log

object MyLogger {
    private const val DEBUG = true
    private const val PREFIX = "My_Logs"

    fun logThis(tag: String, from: String, message: String, exception: Exception? = null) {
        if (DEBUG)
            Log.d(
                    "$PREFIX + $tag",
                    "@ $from --message-- $message",
                    exception
            )
    }
}