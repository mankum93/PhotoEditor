package com.allthingsandroid.android.photoeditor

import android.widget.TextView


class TextGraphicalElement(
    tag: String,
    mPhotoEditorView: PhotoEditorView,
    mMultiTouchListener: MultiTouchListener,
    mViewState: PhotoEditorViewState,
    layoutId: Int,
    viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) : GraphicalBaseWithTextView(
    tag,
    mPhotoEditorView,
    mMultiTouchListener,
    mViewState,
    layoutId,
    ViewType.TEXT,
    viewPlacement
) {
    public override lateinit var contentView: TextView
}