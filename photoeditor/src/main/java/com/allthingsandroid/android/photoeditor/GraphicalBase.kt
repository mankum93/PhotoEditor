package com.allthingsandroid.android.photoeditor

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams


// The name "GraphicalBase": Originally termed as "Graphic" by the lib developers, Graphic
// is supposed to be a representation of graphical data, such as, Bitmap but the responsibilities
// are not true to the concept of a "graphic".
// It is due to this reason, we have changed the name to such. Now, it is a View based
// graphical container
abstract class GraphicalBase(
    protected val context: Context,
    val tag: String,
    val viewType: ViewType,
    internal val viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) {
    lateinit var rootView: View
    protected set
}

sealed class ViewPlacement{
    object DEFAULT: ViewPlacement()
    class LayoutParamsBased(val layoutParams: LayoutParams): ViewPlacement()
}