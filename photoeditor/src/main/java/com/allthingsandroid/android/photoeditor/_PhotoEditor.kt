package com.allthingsandroid.android.photoeditor

import android.Manifest
import android.view.View
import androidx.annotation.RequiresPermission
import com.allthingsandroid.android.photoeditor.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
fun PhotoEditor.saveAsFile(
    imagePath: String,
    saveSettings: SaveSettings,
    onSaveListener: PhotoEditor.OnSaveListener
) {
    GlobalScope.launch(Dispatchers.Main) {
        when (val result = saveAsFile(imagePath, saveSettings)) {
            is SaveFileResult.Success -> onSaveListener.onSuccess(imagePath)
            is SaveFileResult.Failure -> onSaveListener.onFailure(result.exception)
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
fun PhotoEditor.saveAsFile(imagePath: String, onSaveListener: PhotoEditor.OnSaveListener) {
    saveAsFile(imagePath, SaveSettings.Builder().build(), onSaveListener)
}

fun PhotoEditor.saveAsBitmap(saveSettings: SaveSettings, onSaveBitmap: OnSaveBitmap) {
    GlobalScope.launch(Dispatchers.Main) {
        val bitmap = saveAsBitmap(saveSettings)
        onSaveBitmap.onBitmapReady(bitmap)
    }
}

fun PhotoEditor.saveAsBitmap(onSaveBitmap: OnSaveBitmap) {
    saveAsBitmap(SaveSettings.Builder().build(), onSaveBitmap)
}

fun PhotoEditor.boundedBoxSticker(): StickerGraphicalElement {
    val sticker = addImage(
        StickerGraphicalElementBuilder().layoutId(R.layout.bounded_box_iv_based_sticker)
    )
    val closeView = sticker.rootView.findViewById<View>(R.id.close)
    closeView.setOnClickListener {
        removeGraphicalElement(sticker.tag)
    }
    return sticker
}

fun PhotoEditor.boundedBoxText(): TextGraphicalElement {
    val text = addText(
        TextGraphicalElementBuilder().layoutId(R.layout.bounded_box_tv_based_graphical_element)
    )
    val closeView = text.rootView.findViewById<View>(R.id.close)
    closeView.setOnClickListener {
        removeGraphicalElement(text.tag)
    }
    return text
}

fun PhotoEditor.boundedBoxEmoji(): EmojiGraphicalElement {
    val text = addEmoji(
        EmojiGraphicalElementBuilder().layoutId(R.layout.bounded_box_tv_based_graphical_element)
    )
    val closeView = text.rootView.findViewById<View>(R.id.close)
    closeView.setOnClickListener {
        removeGraphicalElement(text.tag)
    }
    return text
}

fun PhotoEditor.defaultTouchBehaviors(): MutableList<BasePhotoEditorTouchListener>{
    return arrayListOf(
        DefaultTranslateTouchListener(true),
        DefaultScaleTouchListener(true)
    )
}