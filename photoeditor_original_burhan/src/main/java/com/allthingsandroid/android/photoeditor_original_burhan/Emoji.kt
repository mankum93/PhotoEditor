package com.allthingsandroid.android.photoeditor_original_burhan

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.allthingsandroid.android.photoeditor_original_burhan.R


internal class Emoji(
    private val mPhotoEditorView: PhotoEditorViewOriginal,
    private val mMultiTouchListener: MultiTouchListenerOriginal,
    private val mViewState: PhotoEditorViewStateOriginal,
    graphicManager: GraphicManagerOriginal?,
    private val mDefaultEmojiTypeface: Typeface?
) : Graphic(
    context = mPhotoEditorView.context,
    graphicManager = graphicManager,
    viewType = ViewType.EMOJI,
    layoutId = R.layout.view_photo_editor_text
) {
    private var txtEmoji: TextView? = null
    fun buildView(emojiTypeface: Typeface?, emojiName: String?) {
        txtEmoji?.apply {
            if (emojiTypeface != null) {
                typeface = emojiTypeface
            }
            textSize = 56f
            text = emojiName
        }
    }

    private fun setupGesture() {
        val onGestureControl = buildGestureController(mPhotoEditorView, mViewState)
        mMultiTouchListener.setOnGestureControl(onGestureControl)
        val rootView = rootView
        rootView.setOnTouchListener(mMultiTouchListener)
    }

    override fun setupView(rootView: View) {
        txtEmoji = rootView.findViewById(R.id.tvPhotoEditorText)
        txtEmoji?.run {
            if (mDefaultEmojiTypeface != null) {
                typeface = mDefaultEmojiTypeface
            }
            gravity = Gravity.CENTER
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    init {
        setupGesture()
    }
}