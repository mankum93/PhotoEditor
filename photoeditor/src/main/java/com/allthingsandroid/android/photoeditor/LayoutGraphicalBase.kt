package com.allthingsandroid.android.photoeditor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes


abstract class LayoutGraphicalBase(
    context: Context,
    tag: String,
    /**
     * The layout id of the content to be placed on top of the source image. This would
     * typically be a sticker, text, emoji, etc
     */
    @LayoutRes
    protected val layoutId: Int,
    viewType: ViewType,
    viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) : GraphicalBase(
    context,
    tag,
    viewType,
    viewPlacement
) {

    init {
        if (layoutId == 0) {
            throw UnsupportedOperationException("Layout id cannot be zero. Please define a layout")
        }
        rootView = LayoutInflater.from(context).inflate(layoutId, null)
        setup(rootView)
    }


    private fun setup(rootView: View) {
        //We are setting tag as ViewType to identify what type of the view it is
        //when we remove the view from stack i.e onRemoveViewListener(ViewType viewType, int numberOfAddedViews);
        rootView.tag = viewType
    }
}