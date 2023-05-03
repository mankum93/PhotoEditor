package com.allthingsandroid.android.photoeditor_original_burhan


class BrushDrawingStateListenerOriginal internal constructor(
    private val mPhotoEditorView: PhotoEditorViewOriginal,
    private val mViewState: PhotoEditorViewStateOriginal
) : BrushViewChangeListenerOriginal {
    private var mOnPhotoEditorListener: OnPhotoEditorListener? = null
    fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener?) {
        mOnPhotoEditorListener = onPhotoEditorListener
    }

    override fun onViewAdd(drawingView: DrawingViewOriginal) {
        if (mViewState.redoViewsCount > 0) {
            mViewState.popRedoView()
        }
        mViewState.addAddedView(drawingView)
        mOnPhotoEditorListener?.onAddViewListener(
            ViewType.BRUSH_DRAWING,
            mViewState.addedViewsCount
        )
    }

    override fun onViewRemoved(drawingView: DrawingViewOriginal) {
        if (mViewState.addedViewsCount > 0) {
            val removeView = mViewState.removeAddedView(
                mViewState.addedViewsCount - 1
            )
            if (removeView !is DrawingViewOriginal) {
                mPhotoEditorView.removeView(removeView)
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