package com.allthingsandroid.android.photoeditor

import android.widget.ImageView

open abstract class BaseScaleTouchListener: BasePhotoEditorTouchListener {

    var mIsPinchScalable: Boolean = true

    protected val minimumScale = 0.5f
    protected val maximumScale = 10.0f

    constructor(mIsPinchScalable: Boolean) : super() {
        this.mIsPinchScalable = mIsPinchScalable
    }

    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        sourceImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState,
        mIsPinchScalable: Boolean
    ) : super(photoEditor, photoEditorView, sourceImageView, onPhotoEditorListener, viewState) {
        this.mIsPinchScalable = mIsPinchScalable
    }
}