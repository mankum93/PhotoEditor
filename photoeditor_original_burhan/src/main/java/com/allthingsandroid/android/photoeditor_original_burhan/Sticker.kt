package com.allthingsandroid.android.photoeditor_original_burhan

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.allthingsandroid.android.photoeditor_original_burhan.R


internal class Sticker(
    private val mPhotoEditorView: PhotoEditorViewOriginal,
    private val mMultiTouchListener: MultiTouchListenerOriginal,
    private val mViewState: PhotoEditorViewStateOriginal,
    graphicManager: GraphicManagerOriginal?
) : Graphic(
    context = mPhotoEditorView.context,
    graphicManager = graphicManager,
    viewType = ViewType.IMAGE,
    layoutId = R.layout.view_photo_editor_image
) {
    private var imageView: ImageView? = null
    fun buildView(desiredImage: Bitmap?) {
        imageView?.setImageBitmap(desiredImage)
    }

    private fun setupGesture() {
        val onGestureControl = buildGestureController(mPhotoEditorView, mViewState)
        mMultiTouchListener.setOnGestureControl(onGestureControl)
        val rootView = rootView
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    override fun setupView(rootView: View) {
        imageView = rootView.findViewById(R.id.imgPhotoEditorImage)
    }

    init {
        setupGesture()
    }
}