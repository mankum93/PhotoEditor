package com.allthingsandroid.android.photoeditor

import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

class DefaultTranslateTouchListener: BasePhotoEditorTouchListener {

    private var mActivePointerId = MultiTouchListener.INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f

    var mIsPinchScalable: Boolean = true

    private val mNoOpScaleGestureDetector = ScaleGestureDetector(object:
        ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
            return !mIsPinchScalable
        }

        override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
            return mIsPinchScalable
        }
    })

    constructor(mIsPinchScalable: Boolean) : super() {
        this.mIsPinchScalable = mIsPinchScalable
    }

    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        sourceImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState,
        mIsPinchScalable: Boolean
    ) : super(photoEditor, photoEditorView, sourceImageView, onPhotoEditorListener, viewState) {
        this.mIsPinchScalable = mIsPinchScalable
    }


    override fun onTouch(tag: String, view: View, event: MotionEvent): Boolean {
        mNoOpScaleGestureDetector.onTouchEvent(view, event)
        if (!isTranslateEnabled) {
            return true
        }
        val action = event.action
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                mPrevRawX = event.rawX
                mPrevRawY = event.rawY
                mActivePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE ->
                // Only enable dragging on focused stickers.
                if (view === viewState.currentSelectedView?.rootView) {
                    val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                    if (pointerIndexMove != -1) {
                        val currX = event.getX(pointerIndexMove)
                        val currY = event.getY(pointerIndexMove)
                        if (!mNoOpScaleGestureDetector.isInProgress) {
                            MultiTouchListener.adjustTranslation(
                                view,
                                currX - mPrevX,
                                currY - mPrevY
                            )
                        }
                    }
                }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = MultiTouchListener.INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = MultiTouchListener.INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndexPointerUp =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndexPointerUp)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndexPointerUp == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }
}