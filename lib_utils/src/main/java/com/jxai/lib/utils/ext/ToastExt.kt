package com.jxai.lib.utils.ext

import android.content.Context
import android.widget.Toast

fun Context.toastShortShow(charSequence:CharSequence){
    Toast.makeText(applicationContext,charSequence,Toast.LENGTH_SHORT).show()
}


fun Context.toastLongShow(charSequence: CharSequence){
    Toast.makeText(applicationContext,charSequence,Toast.LENGTH_LONG).show()
}