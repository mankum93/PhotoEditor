package com.allthingsandroid.android.photoeditor


abstract class GraphicalBaseWithDrawingView(
    tag: String,
    protected val mPhotoEditorView: PhotoEditorView,
    protected val mViewState: PhotoEditorViewState,
) : GraphicalBase(
    context = mPhotoEditorView.context,
    tag = tag,
    viewType = ViewType.BRUSH_DRAWING,
) {
    protected open var contentView: DrawingView? = null
        set(value) {
            field = value
            if(value != null){
                rootView = value
            }
        }
}