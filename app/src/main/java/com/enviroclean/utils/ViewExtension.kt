package com.enviroclean.utils

import android.content.ContextWrapper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun View.getParentActivity(): AppCompatActivity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}


/**
 * To get string value directly from TextInputEditText
 */
fun TextInputEditText.getTextValue(): String {
    return this.text.toString().trim()
}


/**
 * To get string value directly from EditText
 */
fun EditText.getTextValue(): String {
    return this.text.toString().trim()
}

/**
 * This is an extension function created for all text watchers used in any activity
 */
fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            cb(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun TextInputLayout.nonEditable(isClickable: Boolean) {
    this.isFocusable = isClickable
    this.isClickable = isClickable
    this.isFocusableInTouchMode = isClickable

    this.editText?.isFocusable = isClickable
    this.editText?.isClickable = isClickable
    this.editText?.isFocusableInTouchMode = isClickable
}