package com.allthingsandroid.android.photoeditor

import android.view.MotionEvent
import android.view.View

interface OnTouchListener {

    fun onTouch(tag: String, view: View, event: MotionEvent): Boolean
}