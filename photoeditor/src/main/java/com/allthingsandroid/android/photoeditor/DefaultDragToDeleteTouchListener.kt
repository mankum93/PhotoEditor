package com.allthingsandroid.android.photoeditor

import android.view.View
import android.widget.ImageView
import com.crazylegend.view.dpToPx

/**
 * Created on 18/01/2017.
 *
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 *
 *
 */
class DefaultDragToDeleteTouchListener: DragToDeleteTouchListener {

    constructor(
        photoEditor: PhotoEditor,
        photoEditorView: PhotoEditorView,
        photoEditImageView: ImageView,
        onPhotoEditorListener: OnPhotoEditorListener?,
        viewState: PhotoEditorViewState,
        deleteView: View,
        eventsListener: DragToDeleteTouchListener.EventsListener?
    ) : super(
        photoEditor,
        photoEditorView,
        photoEditImageView,
        onPhotoEditorListener,
        viewState,
        deleteView,
        EventsListenerWrapper(eventsListener)
    )

    constructor(
        deleteView: View,
        eventsListener: DragToDeleteTouchListener.EventsListener?
    ) : super(deleteView, EventsListenerWrapper(eventsListener))


    private class EventsListenerWrapper(
        private val eventsListener: EventsListener?): EventsListener{

        private val originalScale = FloatArray(2)

        override fun onTouchIntersectDeleteView(tag: String, view: View, positionRelToDeleteView: DragToDeleteTouchListener.PositionRelToDeleteView) {
            if(positionRelToDeleteView == DragToDeleteTouchListener.PositionRelToDeleteView.ENTERING_DELETE_VIEW){

                view.let{
                    originalScale[0] = it.scaleX
                    originalScale[1] = it.scaleY
                }
                view.apply {
                    scaleX = 70.dpToPx(context)/width
                    scaleY = 70.dpToPx(context)/height
                }
            }
            else if(positionRelToDeleteView == DragToDeleteTouchListener.PositionRelToDeleteView.EXITING_DELETE_VIEW){
                view.apply {
                    scaleX = originalScale[0]
                    scaleY = originalScale[1]
                }
            }
            eventsListener?.onTouchIntersectDeleteView(tag, view, positionRelToDeleteView)
        }

        override fun onTouchEndOverDeleteView(tag: String, view: View) {
            eventsListener?.onTouchEndOverDeleteView(tag, view)
        }
    }
}