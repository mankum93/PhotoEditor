package com.allthingsandroid.android.photoeditor_original_burhan

/**
 * Created on 1/17/2018.
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 *
 *
 */
interface BrushViewChangeListenerOriginal {
    fun onViewAdd(drawingView: DrawingViewOriginal)
    fun onViewRemoved(drawingView: DrawingViewOriginal)
    fun onStartDrawing()
    fun onStopDrawing()
}