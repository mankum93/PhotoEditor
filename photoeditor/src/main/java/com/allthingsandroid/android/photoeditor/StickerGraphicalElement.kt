package com.allthingsandroid.android.photoeditor

import android.widget.ImageView


class StickerGraphicalElement(
    tag: String,
    mPhotoEditorView: PhotoEditorView,
    mMultiTouchListener: MultiTouchListener,
    mViewState: PhotoEditorViewState,
    layoutId: Int,
    viewPlacement: ViewPlacement = ViewPlacement.DEFAULT
) : GraphicalBaseWithImageView(
    tag,
    mPhotoEditorView,
    mMultiTouchListener,
    mViewState,
    layoutId,
    viewPlacement
) {
    public override lateinit var contentView: ImageView
}