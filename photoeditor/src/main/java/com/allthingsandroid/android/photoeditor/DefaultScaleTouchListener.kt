package com.allthingsandroid.android.photoeditor

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

class DefaultScaleTouchListener: BaseScaleTouchListener {

    private val mScaleGestureDetector: ScaleGestureDetector

    constructor(mIsPinchScalable: Boolean) : super(mIsPinchScalable)
    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        sourceImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState,
        mIsPinchScalable: Boolean
    ) : super(
        photoEditor,
        photoEditorView,
        sourceImageView,
        onPhotoEditorListener,
        viewState,
        mIsPinchScalable
    )


    init {
        mScaleGestureDetector = ScaleGestureDetector(ScaleGestureListener())
    }

    override fun onTouch(tag: String, view: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(view, event)
        return true
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var mPivotX = 0f
        private var mPivotY = 0f
        private val mPrevSpanVector = Vector2D()

        override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
            mPivotX = detector.getFocusX()
            mPivotY = detector.getFocusY()
            mPrevSpanVector.set(detector.getCurrentSpanVector())
            return mIsPinchScalable
        }

        override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
            val info = TransformInfo()
            info.deltaScale = if (isScaleEnabled) detector.getScaleFactor() else 1.0f
            info.deltaAngle = if (isRotateEnabled) Vector2D.getAngle(
                mPrevSpanVector,
                detector.getCurrentSpanVector()
            ) else 0.0f
            info.deltaX = if (isTranslateEnabled) detector.getFocusX() - mPivotX else 0.0f
            info.deltaY = if (isTranslateEnabled) detector.getFocusY() - mPivotY else 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            MultiTouchListener.move(view, info)
            return !mIsPinchScalable
        }
    }

}