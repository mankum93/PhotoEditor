package com.allthingsandroid.android.photoeditor

import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.allthingsandroid.android.photoeditor.R


open class GraphicalBaseWithTextView(
    tag: String,
    protected val mPhotoEditorView: PhotoEditorView,
    protected val mMultiTouchListener: MultiTouchListener,
    protected val mViewState: PhotoEditorViewState,
    layoutId: Int,
    viewType: ViewType,
    viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) : LayoutGraphicalBase(
    context = mPhotoEditorView.context,
    tag = tag,
    viewType = viewType,
    layoutId = layoutId,
    viewPlacement = viewPlacement
) {

    protected open lateinit var contentView: TextView

    init {
        touchHandlersSetup()
        setupView(rootView)
    }

    private fun touchHandlersSetup() {
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    private fun setupView(rootView: View) {
        contentView = rootView.findViewById(R.id.content)
        contentView.run {
            gravity = Gravity.CENTER
        }
    }
}