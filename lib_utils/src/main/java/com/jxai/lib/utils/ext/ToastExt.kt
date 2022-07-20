package com.jxai.lib.utils.ext

import android.content.Context
import android.widget.Toast

fun Context.toastShort(charSequence: CharSequence) {
    Toast.makeText(applicationContext, charSequence, Toast.LENGTH_SHORT).show()
}


fun Context.toastLong(charSequence: CharSequence) {
    Toast.makeText(applicationContext, charSequence, Toast.LENGTH_LONG).show()
}