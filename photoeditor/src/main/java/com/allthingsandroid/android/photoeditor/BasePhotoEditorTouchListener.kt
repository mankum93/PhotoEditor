package com.allthingsandroid.android.photoeditor

import android.widget.ImageView

open abstract class BasePhotoEditorTouchListener(): OnTouchListener {

    lateinit var photoEditor: PhotoEditor
        internal set
    lateinit var photoEditorView: PhotoEditorView
        internal set
    lateinit var sourceImageView: ImageView
        internal set
    var mOnPhotoEditorListener: OnPhotoEditorListener? = null
        internal set
    lateinit var viewState: PhotoEditorViewState
        internal set

    var isRotateEnabled = true
    var isTranslateEnabled = true
    var isScaleEnabled = true

    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        sourceImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState
    ) : this() {
        this.photoEditor = photoEditor
        this.photoEditorView = photoEditorView
        this.sourceImageView = sourceImageView
        this.mOnPhotoEditorListener = onPhotoEditorListener
        this.viewState = viewState
    }


}