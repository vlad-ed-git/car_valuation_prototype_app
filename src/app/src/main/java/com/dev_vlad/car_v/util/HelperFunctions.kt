package com.dev_vlad.car_v.util

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.inputmethod.InputMethodManager

fun hideKeyBoard(context: Context, v: View) {
    val imm = context.getSystemService(InputMethodManager::class.java) as InputMethodManager?
    imm?.hideSoftInputFromWindow(v.windowToken, 0)
}