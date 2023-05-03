package com.allthingsandroid.android.photoeditor_original_burhan.shape

/**
 * The different kind of known Shapes.
 */
sealed interface ShapeType {

    object Brush : ShapeType
    object Oval : ShapeType
    object Rectangle : ShapeType
    object Line : ShapeType
    class Arrow(val pointerLocation: ArrowPointerLocation = ArrowPointerLocation.START) : ShapeType

}