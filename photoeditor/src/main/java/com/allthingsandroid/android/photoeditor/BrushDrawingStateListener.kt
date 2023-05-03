package com.allthingsandroid.android.photoeditor


class BrushDrawingStateListener internal constructor(
    private val mPhotoEditorView: PhotoEditorView,
    private val mViewState: PhotoEditorViewState
) : BrushViewChangeListener {
    private var mOnPhotoEditorListener: OnPhotoEditorListener? = null
    fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener?) {
        mOnPhotoEditorListener = onPhotoEditorListener
    }

    override fun onViewAdd(drawingGraphicalElement: DrawingGraphicalElement) {
        if (mViewState.redoViewsCount > 0) {
            mViewState.popRedoView()
        }
        mViewState.addAddedView(drawingGraphicalElement)
        mOnPhotoEditorListener?.onAddViewListener(
            ViewType.BRUSH_DRAWING,
            mViewState.addedViewsCount
        )
    }

    override fun onViewRemoved(drawingGraphicalElement: DrawingGraphicalElement) {
        if (mViewState.addedViewsCount > 0) {
            val removeView = mViewState.removeAddedView(
                mViewState.addedViewsCount - 1
            )
            if (drawingGraphicalElement.contentView !is DrawingView) {
                mPhotoEditorView.removeView(drawingGraphicalElement.contentView)
            }
            mViewState.pushRedoView(removeView)
        }
        mOnPhotoEditorListener?.onRemoveViewListener(
            ViewType.BRUSH_DRAWING,
            mViewState.addedViewsCount
        )
    }

    override fun onStartDrawing() {
        mOnPhotoEditorListener?.onStartViewChangeListener(ViewType.BRUSH_DRAWING)

    }

    override fun onStopDrawing() {
        mOnPhotoEditorListener?.onStopViewChangeListener(ViewType.BRUSH_DRAWING)
    }
}