package com.allthingsandroid.android.photoeditor


class DrawingGraphicalElement(
    tag: String,
    mPhotoEditorView: PhotoEditorView,
    mViewState: PhotoEditorViewState,
) : GraphicalBaseWithDrawingView(
    tag,
    mPhotoEditorView,
    mViewState,
) {
    public override var contentView: DrawingView? = null
    set(value) {
        field = value
        super.contentView = value
    }
}