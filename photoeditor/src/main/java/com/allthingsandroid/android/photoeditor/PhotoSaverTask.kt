package com.allthingsandroid.android.photoeditor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.allthingsandroid.android.photoeditor.common.BitmapUtil.removeTransparency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


internal class PhotoSaverTask(
    private val photoEditor: PhotoEditor,
    private val photoEditorView: PhotoEditorView,
    private val drawingGraphicalElement: DrawingGraphicalElement,
    private val boxHelper: BoxHelper,
    private var saveSettings: SaveSettings
) {

    private val drawingView: DrawingView? = drawingGraphicalElement.contentView

    private fun onBeforeSaveImage() {
        boxHelper.clearHelperBox()
        drawingView?.destroyDrawingCache()
    }

    fun saveImageAsBitmap(): Bitmap {
        onBeforeSaveImage()
        val bitmap = buildBitmap()
        if (saveSettings.isClearViewsEnabled) {
            photoEditor.clearAllViews()
        }
        return bitmap
    }

    suspend fun saveImageAsFile(imagePath: String): SaveFileResult {
        onBeforeSaveImage()
        val capturedBitmap = buildBitmap()

        val result = withContext(Dispatchers.IO) {
            val file = File(imagePath)
            try {
                FileOutputStream(file, false).use { outputStream ->
                    capturedBitmap.compress(
                        saveSettings.compressFormat,
                        saveSettings.compressQuality,
                        outputStream
                    )
                    outputStream.flush()
                }

                SaveFileResult.Success
            } catch (e: IOException) {
                SaveFileResult.Failure(e)
            }
        }

        if (result is SaveFileResult.Success) {
            // Clear all views if it's enabled in save settings
            if (saveSettings.isClearViewsEnabled) {
                photoEditor.clearAllViews()
            }
        }

        return result
    }

    private fun buildBitmap(): Bitmap {
        return if (saveSettings.isTransparencyEnabled) {
            removeTransparency(captureView(photoEditorView))
        } else {
            captureView(photoEditorView)
        }
    }

    private fun captureView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    companion object {
        const val TAG = "PhotoSaverTask"
    }

}