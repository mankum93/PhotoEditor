package com.allthingsandroid.android.photoeditor

import android.view.View
import android.widget.TextView


class EmojiGraphicalElement(
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
    ViewType.EMOJI,
    viewPlacement
) {
    public override lateinit var contentView: TextView

    init{
        setupView(rootView)
    }

    private fun setupView(rootView: View) {
        contentView.run {
            textSize = 56f
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }
}