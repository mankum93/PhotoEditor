package com.allthingsandroid.android.photoeditor_original_burhan

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.allthingsandroid.android.photoeditor_original_burhan.R


internal class Text(
    private val mPhotoEditorView: PhotoEditorViewOriginal,
    private val mMultiTouchListener: MultiTouchListenerOriginal,
    private val mViewState: PhotoEditorViewStateOriginal,
    private val mDefaultTextTypeface: Typeface?,
    private val mGraphicManager: GraphicManagerOriginal
) : Graphic(
    context = mPhotoEditorView.context,
    graphicManager = mGraphicManager,
    viewType = ViewType.TEXT,
    layoutId = R.layout.view_photo_editor_text
) {
    private var mTextView: TextView? = null
    fun buildView(text: String?, styleBuilder: TextStyleBuilder?) {
        mTextView?.apply {
            this.text = text
            styleBuilder?.applyStyle(this)
        }
    }

    private fun setupGesture() {
        val onGestureControl = buildGestureController(mPhotoEditorView, mViewState)
        mMultiTouchListener.setOnGestureControl(onGestureControl)
        val rootView = rootView
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    override fun setupView(rootView: View) {
        mTextView = rootView.findViewById(R.id.tvPhotoEditorText)
        mTextView?.run {
            gravity = Gravity.CENTER
            typeface = mDefaultTextTypeface
        }
    }

    override fun updateView(view: View?) {
        val textInput = mTextView?.text.toString()
        val currentTextColor = mTextView?.currentTextColor ?: 0
        val photoEditorListener = mGraphicManager.onPhotoEditorListener
        photoEditorListener?.onEditTextChangeListener(view, textInput, currentTextColor)
    }

    init {
        setupGesture()
    }
}