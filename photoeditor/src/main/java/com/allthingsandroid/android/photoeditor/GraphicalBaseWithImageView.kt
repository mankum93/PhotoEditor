package com.allthingsandroid.android.photoeditor

import android.view.View
import android.widget.ImageView
import com.allthingsandroid.android.photoeditor.R


open class GraphicalBaseWithImageView(
    tag: String,
    protected val mPhotoEditorView: PhotoEditorView,
    protected val mMultiTouchListener: MultiTouchListener,
    protected val mViewState: PhotoEditorViewState,
    layoutId: Int,
    viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) : LayoutGraphicalBase(
    context = mPhotoEditorView.context,
    tag = tag,
    viewType = ViewType.IMAGE,
    layoutId = layoutId,
    viewPlacement = viewPlacement
) {

    protected open lateinit var contentView: ImageView

    init {
        touchHandlersSetup()
        setupView(rootView)
    }

    private fun touchHandlersSetup() {
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    private fun setupView(rootView: View) {
        contentView = rootView.findViewById(R.id.content)
    }
}