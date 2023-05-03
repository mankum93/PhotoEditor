package com.allthingsandroid.android.photoeditor

import android.graphics.Rect
import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import android.view.View
import android.widget.ImageView

/**
 * Created on 18/01/2017.
 *
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 *
 *
 */
open class DragToDeleteTouchListener: BasePhotoEditorTouchListener {

    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f

    private val location = IntArray(2)
    private lateinit var outRect: Rect

    private val deleteView: View

    protected var eventsListener: EventsListener? = null

    private var mIsPinchScalable: Boolean = true

    private val mNoOpScaleGestureDetector = ScaleGestureDetector(object:
        ScaleGestureDetector.SimpleOnScaleGestureListener(){
        override fun onScale(view: View, detector: ScaleGestureDetector): Boolean {
            return !mIsPinchScalable
        }

        override fun onScaleBegin(view: View, detector: ScaleGestureDetector): Boolean {
            return mIsPinchScalable
        }
    })

    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        photoEditImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState,
        deleteView: View,
        eventsListener: EventsListener?
    ) : super(photoEditor, photoEditorView, photoEditImageView, onPhotoEditorListener, viewState){
        this.deleteView = deleteView
        this.eventsListener = eventsListener
        init()
    }

    constructor(
        deleteView: View,
        eventsListener: EventsListener?
    ) : super() {
        this.deleteView = deleteView
        this.eventsListener = eventsListener
        init()
    }

    private fun init() {
        outRect = Rect(
            this.deleteView.left, this.deleteView.top,
            this.deleteView.right, this.deleteView.bottom
        )
    }


    override fun onTouch(tag: String, view: View, event: MotionEvent): Boolean {
        mNoOpScaleGestureDetector.onTouchEvent(view, event)
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
                deleteView.visibility = View.VISIBLE
            }
            MotionEvent.ACTION_MOVE ->{
                if (view === viewState.currentSelectedView?.rootView) {
                    val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                    if (pointerIndexMove != -1) {
                        if (!mNoOpScaleGestureDetector.isInProgress) {
                            val currRawX = event.rawX
                            val currRawY = event.rawY

                            // Previously in delete view area, still being in the same
                            val previouslyInDeleteViewArea =
                                isViewInBounds(deleteView, mPrevRawX.toInt(), mPrevRawY.toInt())
                            val currentlyInDeleteViewArea =
                                isViewInBounds(deleteView, currRawX.toInt(), currRawY.toInt())
                            if (previouslyInDeleteViewArea && !currentlyInDeleteViewArea){
                                // Coming out of the Delete view area
                                eventsListener?.onTouchIntersectDeleteView(tag, view, PositionRelToDeleteView.EXITING_DELETE_VIEW,)
                            }
                            else if(!previouslyInDeleteViewArea && currentlyInDeleteViewArea){
                                // Entering into the Delete view area
                                eventsListener?.onTouchIntersectDeleteView(tag, view, PositionRelToDeleteView.ENTERING_DELETE_VIEW,)
                            }
                            mPrevX = event.x
                            mPrevY = event.y
                            mPrevRawX = event.rawX
                            mPrevRawY = event.rawY
                        }
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
                if (isViewInBounds(deleteView, x, y)) {
                    eventsListener?.onTouchEndOverDeleteView(tag, view)
                }
                deleteView.visibility = View.GONE
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

    private fun isViewInBounds(view: View?, x: Int, y: Int): Boolean {
        return view?.run {
            getDrawingRect(outRect)
            getLocationOnScreen(location)
            outRect.offset(location[0], location[1])
            outRect.contains(x, y)
        } ?: false
    }

    interface EventsListener {
        fun onTouchIntersectDeleteView(tag: String, view: View, positionRelToDeleteView: PositionRelToDeleteView)
        fun onTouchEndOverDeleteView(tag: String, view: View)
    }

    enum class PositionRelToDeleteView{
        ENTERING_DELETE_VIEW,
        EXITING_DELETE_VIEW,
    }
}