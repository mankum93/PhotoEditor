package com.allthingsandroid.android.photoeditor

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min

class MultiTouchListener(
    tag: String,
    photoEditor: PhotoEditor,
    photoEditorView: PhotoEditorView,
    sourceImageView: ImageView,
    private val mIsPinchScalable: Boolean,
    onPhotoEditorListener: OnPhotoEditorListener?,
    viewState: PhotoEditorViewState,
    defaultTouchBehavior: Boolean,
    touchListeners: List<BasePhotoEditorTouchListener>? = null
) : OnTouchListener {

    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f

    //private val mScaleGestureDetector: ScaleGestureDetector
    private val location = IntArray(2)
    private var outRect: Rect? = null

    private val tag: String
    private val photoEditor: PhotoEditor
    private val photoEditImageView: ImageView
    private val photoEditorView: PhotoEditorView
    private val mOnPhotoEditorListener: OnPhotoEditorListener?
    private val viewState: PhotoEditorViewState

    private val defaultTouchBehavior: Boolean

    private val touchListeners = arrayListOf<BasePhotoEditorTouchListener>()

    init {
        this.defaultTouchBehavior = defaultTouchBehavior
        if (this.defaultTouchBehavior) {
            this.touchListeners.add(
                DefaultScaleTouchListener(
                    photoEditor,
                    photoEditorView,
                    sourceImageView,
                    onPhotoEditorListener,
                    viewState,
                    mIsPinchScalable
                )
            )
            this.touchListeners.add(
                DefaultTranslateTouchListener(
                    photoEditor,
                    photoEditorView,
                    sourceImageView,
                    onPhotoEditorListener,
                    viewState,
                    mIsPinchScalable
                )
            )
        }
        else if(!touchListeners.isNullOrEmpty()){
            this.touchListeners.apply {
                addAll(touchListeners)
                forEach {
                    it.viewState = viewState
                    it.sourceImageView = sourceImageView
                    it.photoEditorView = photoEditorView
                    it.mOnPhotoEditorListener = onPhotoEditorListener
                }
            }
        }
        else{
            // The View will stay fixed at one place. No motion possible
        }

        this.tag = tag
        this.photoEditor = photoEditor
        this.photoEditorView = photoEditorView
        this.photoEditImageView = sourceImageView
        mOnPhotoEditorListener = onPhotoEditorListener
        outRect = Rect(0, 0, 0, 0)
        this.viewState = viewState
    }


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        this.touchListeners.forEach {
            it.onTouch(tag, view, event)
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
                //view.bringToFront()
                firePhotoEditorSDKListener(view, true)
            }
            MotionEvent.ACTION_MOVE ->{

            }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                if (!isViewInBounds(photoEditImageView, x, y)) {
                    view.animate().translationY(0f).translationY(0f)
                }
                firePhotoEditorSDKListener(view, false)
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

    fun addTouchHandler(touchHandler: BasePhotoEditorTouchListener){
        this.touchListeners.apply {
            touchHandler.let {
                it.viewState = viewState
                it.sourceImageView = this@MultiTouchListener.photoEditImageView
                it.photoEditorView = photoEditorView
                it.mOnPhotoEditorListener = this@MultiTouchListener.mOnPhotoEditorListener
            }
            add(touchHandler)
        }
    }

    private fun firePhotoEditorSDKListener(view: View, isStart: Boolean) {
        val viewTag = view.tag
        if (mOnPhotoEditorListener != null && viewTag != null && viewTag is ViewType) {
            if (isStart) mOnPhotoEditorListener.onStartViewChangeListener(view.tag as ViewType) else mOnPhotoEditorListener.onStopViewChangeListener(
                view.tag as ViewType
            )
        }
    }

    private fun isViewInBounds(view: View?, x: Int, y: Int): Boolean {
        return view?.run {
            getDrawingRect(outRect)
            getLocationOnScreen(location)
            outRect?.offset(location[0], location[1])
            outRect?.contains(x, y)
        } ?: false
    }
    companion object {
        internal const val INVALID_POINTER_ID = -1
        internal fun adjustAngle(degrees: Float): Float {
            return when {
                degrees > 180.0f -> {
                    degrees - 360.0f
                }
                degrees < -180.0f -> {
                    degrees + 360.0f
                }
                else -> degrees
            }
        }

        internal fun move(view: View, info: TransformInfo) {
            computeRenderOffset(view, info.pivotX, info.pivotY)
            adjustTranslation(view, info.deltaX, info.deltaY)
            var scale = view.scaleX * info.deltaScale
            scale = max(info.minimumScale, min(info.maximumScale, scale))
            view.scaleX = scale
            view.scaleY = scale
            val rotation = adjustAngle(view.rotation + info.deltaAngle)
            view.rotation = rotation
        }

        internal fun adjustTranslation(view: View, deltaX: Float, deltaY: Float) {
            val deltaVector = floatArrayOf(deltaX, deltaY)
            view.matrix.mapVectors(deltaVector)
            view.translationX = view.translationX + deltaVector[0]
            view.translationY = view.translationY + deltaVector[1]
        }

        internal fun computeRenderOffset(view: View, pivotX: Float, pivotY: Float) {
            if (view.pivotX == pivotX && view.pivotY == pivotY) {
                return
            }
            val prevPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(prevPoint)
            view.pivotX = pivotX
            view.pivotY = pivotY
            val currPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(currPoint)
            val offsetX = currPoint[0] - prevPoint[0]
            val offsetY = currPoint[1] - prevPoint[1]
            view.translationX = view.translationX - offsetX
            view.translationY = view.translationY - offsetY
        }
    }
}