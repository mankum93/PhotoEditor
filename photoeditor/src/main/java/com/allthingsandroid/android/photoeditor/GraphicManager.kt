package com.allthingsandroid.android.photoeditor

import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import java.lang.IllegalStateException


internal class GraphicManager(
    private val mPhotoEditorView: PhotoEditorView,
    private val mViewState: PhotoEditorViewState
) {
    var onPhotoEditorListener: OnPhotoEditorListener? = null
    fun addView(graphic: GraphicalBase) {
        val view = graphic.rootView

        val params: RelativeLayout.LayoutParams
        when(graphic.viewPlacement){
            ViewPlacement.DEFAULT -> {
                params = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
            is ViewPlacement.LayoutParamsBased -> {
                graphic.viewPlacement.layoutParams.let{
                    if(it !is RelativeLayout.LayoutParams){
                        throw IllegalStateException("View placement supports only RelativeLayout.LayoutParams." +
                                "Supplied layout params are of the type ${it::class.qualifiedName ?: it::class.toString()}")
                    }
                    else{
                        params = it
                    }
                }
            }
        }

        mPhotoEditorView.addView(view, params)
        mViewState.addAddedView(graphic)

        onPhotoEditorListener?.onAddViewListener(
            graphic.viewType,
            mViewState.addedViewsCount
        )
    }

    fun removeView(graphic: GraphicalBase) {
        val view = graphic.rootView
        if (mViewState.containsAddedView(graphic)) {
            mPhotoEditorView.removeView(view)
            mViewState.removeAddedView(graphic)
            mViewState.pushRedoView(graphic)
            onPhotoEditorListener?.onRemoveViewListener(
                graphic.viewType,
                mViewState.addedViewsCount
            )
        }
    }

    fun updateView(graphic: GraphicalBase) {
        val view = graphic.rootView
        mPhotoEditorView.updateViewLayout(view, view.layoutParams)
        mViewState.replaceAddedView(graphic)
    }

    fun undoView(): Boolean {
        if (mViewState.addedViewsCount > 0) {
            val graphic = mViewState.getAddedView(
                mViewState.addedViewsCount - 1
            )
            val removeView = graphic.rootView
            if (removeView is DrawingView) {
                return removeView.undo()
            } else {
                mViewState.removeAddedView(mViewState.addedViewsCount - 1)
                mPhotoEditorView.removeView(removeView)
                mViewState.pushRedoView(graphic)
            }
            when (val viewTag = removeView.tag) {
                is ViewType -> onPhotoEditorListener?.onRemoveViewListener(
                    viewTag,
                    mViewState.addedViewsCount
                )
            }
        }
        return mViewState.addedViewsCount != 0
    }

    fun redoView(): Boolean {
        if (mViewState.redoViewsCount > 0) {
            val graphic = mViewState.getRedoView(
                mViewState.redoViewsCount - 1
            )
            val redoView = graphic.rootView
            if (redoView is DrawingView) {
                return redoView.redo()
            } else {
                mViewState.popRedoView()
                mPhotoEditorView.addView(redoView)
                mViewState.addAddedView(graphic)
            }
            when (val viewTag = redoView.tag) {
                is ViewType -> onPhotoEditorListener?.onAddViewListener(
                    viewTag,
                    mViewState.addedViewsCount
                )
            }
        }
        return mViewState.redoViewsCount != 0
    }
}