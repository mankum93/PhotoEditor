package com.allthingsandroid.android.photoeditor

/**
 * Created on 1/17/2018.
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 *
 *
 */
interface BrushViewChangeListener {
    fun onViewAdd(drawingGraphicalElement: DrawingGraphicalElement)
    fun onViewRemoved(drawingGraphicalElement: DrawingGraphicalElement)
    fun onStartDrawing()
    fun onStopDrawing()
}