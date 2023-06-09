package com.allthingsandroid.android.photoeditor_original_burhan.shape

import android.graphics.Canvas
import android.graphics.Paint

interface Shape {
    fun draw(canvas: Canvas, paint: Paint)
    fun startShape(x: Float, y: Float)
    fun moveShape(x: Float, y: Float)
    fun stopShape()
}