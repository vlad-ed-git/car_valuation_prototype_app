package com.dev_vlad.car_v.util

import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.myTxt(v: TextInputLayout): String? {
    return v.editText?.text?.toString()
}

fun TextInputLayout.setTxt(v: TextInputLayout, txt : String) {
    v.editText?.setText(txt)
}
