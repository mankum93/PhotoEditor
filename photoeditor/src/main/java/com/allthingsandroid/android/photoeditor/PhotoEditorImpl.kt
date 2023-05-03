package com.allthingsandroid.android.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.GestureDetector
import android.view.View
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import com.allthingsandroid.android.photoeditor.PhotoEditorImageViewListener.OnSingleTapUpCallback
import com.allthingsandroid.android.photoeditor.shape.ShapeBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.sis.util.collection.WeakValueHashMap

/**
 *
 *
 * This class in initialize by [PhotoEditorOriginal.Builder] using a builder pattern with multiple
 * editing attributes
 *
 *
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.1
 * @since 18/01/2017
 */
internal class PhotoEditorImpl @SuppressLint("ClickableViewAccessibility") constructor(
    builder: PhotoEditor.Builder
) : PhotoEditor {
    private val photoEditorView: PhotoEditorView = builder.photoEditorView
    private val viewState: PhotoEditorViewState = PhotoEditorViewState()
    private val imageView: ImageView = builder.imageView
    private val drawingGraphicalElement: DrawingGraphicalElement =
        DrawingGraphicalElement("drawing_view", photoEditorView, viewState).apply {
            contentView = builder.drawingView
        }
    private val drawingView = drawingGraphicalElement.contentView
    private val mBrushDrawingStateListener: BrushDrawingStateListener =
        BrushDrawingStateListener(builder.photoEditorView, viewState)
    private val mBoxHelper: BoxHelper = BoxHelper(builder.photoEditorView, viewState)
    private var mOnPhotoEditorListener: OnPhotoEditorListener? = null
    private val isTextPinchScalable: Boolean = builder.isTextPinchScalable
    private val mDefaultEmojiTypeface: Typeface? = builder.emojiTypeface
    private val mGraphicManager: GraphicManager = GraphicManager(builder.photoEditorView, viewState)
    private val context: Context = builder.context

    private val graphicalElements = WeakValueHashMap<String, GraphicalBase>(String::class.java, false)

    override fun getGraphicalElement(tag: String): GraphicalBase? {
        val element = graphicalElements[tag]
        var found: Boolean = false
        element?.let {
            found = viewState.containsAddedView(it)
        }
        return if(found) {element} else {null}
    }

    override fun removeGraphicalElement(tag: String): Boolean {
        val element = graphicalElements[tag]
        var found: Boolean = false
        element?.let {
            found = viewState.containsAddedView(it)
            mGraphicManager.removeView(it)
        }
        return found
    }

    override fun addImage(stickerBuilder: StickerGraphicalElementBuilder): StickerGraphicalElement {
        val sticker = stickerBuilder.build(
            this, photoEditorView, viewState, mOnPhotoEditorListener, imageView
        )
        addToEditor(sticker)
        graphicalElements[sticker.tag] = sticker
        return sticker
    }

    override fun addText(textGraphicalElementBuilder: TextGraphicalElementBuilder): TextGraphicalElement {
        val text = textGraphicalElementBuilder.build(
            this, photoEditorView, viewState, mOnPhotoEditorListener, imageView
        )
        addToEditor(text)
        graphicalElements[text.tag] = text
        return text
    }

    override fun addEmoji(emojiGraphicalElementBuilder: EmojiGraphicalElementBuilder): EmojiGraphicalElement {
        val text = emojiGraphicalElementBuilder.build(
            this, photoEditorView, viewState, mOnPhotoEditorListener, imageView
        )
        addToEditor(text)
        graphicalElements[text.tag] = text
        return text
    }

    private fun addToEditor(graphic: GraphicalBase) {
        mGraphicManager.addView(graphic)
        // Change the in-focus view
        viewState.currentSelectedView = graphic
    }

    override fun setBrushDrawingMode(brushDrawingMode: Boolean) {
        drawingView?.enableDrawing(brushDrawingMode)
    }

    override val brushDrawableMode: Boolean
        get() = drawingView != null && drawingView.isDrawingEnabled

    override fun setOpacity(@IntRange(from = 0, to = 100) opacity: Int) {
        var opacityValue = opacity
        opacityValue = (opacityValue / 100.0 * 255.0).toInt()
        drawingView?.currentShapeBuilder?.withShapeOpacity(opacityValue)
    }

    override var brushSize: Float
        get() = drawingView?.currentShapeBuilder?.shapeSize ?: 0f
        set(size) {
            drawingView?.currentShapeBuilder?.withShapeSize(size)
        }
    override var brushColor: Int
        get() = drawingView?.currentShapeBuilder?.shapeColor ?: 0
        set(color) {
            drawingView?.currentShapeBuilder?.withShapeColor(color)
        }

    override fun setBrushEraserSize(brushEraserSize: Float) {
        drawingView?.eraserSize = brushEraserSize
    }

    override val eraserSize: Float
        get() = drawingView?.eraserSize ?: 0f

    override fun brushEraser() {
        drawingView?.brushEraser()
    }

    override fun undo(): Boolean {
        return mGraphicManager.undoView()
    }

    override fun redo(): Boolean {
        return mGraphicManager.redoView()
    }

    override fun clearAllViews() {
        clearAllViews(drawingGraphicalElement)
    }

    private fun clearAllViews(graphicalElement: DrawingGraphicalElement) {
        for (i in 0 until viewState.addedViewsCount) {
            photoEditorView.removeView(viewState.getAddedView(i).rootView)
        }
        graphicalElement.let {
            if (viewState.containsAddedView(it)) {
                photoEditorView.addView(it.contentView)
            }
        }

        viewState.clearAddedViews()
        viewState.clearRedoViews()
        graphicalElement.contentView?.clearAll()
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    override suspend fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings
    ): SaveFileResult = withContext(Dispatchers.Main) {
        val photoSaverTask = PhotoSaverTask(this@PhotoEditorImpl, photoEditorView, drawingGraphicalElement, mBoxHelper, saveSettings)
        return@withContext photoSaverTask.saveImageAsFile(imagePath)
    }

    override suspend fun saveAsBitmap(
        saveSettings: SaveSettings
    ): Bitmap = withContext(Dispatchers.Main) {
        val photoSaverTask = PhotoSaverTask(this@PhotoEditorImpl, photoEditorView, drawingGraphicalElement, mBoxHelper, saveSettings)
        return@withContext photoSaverTask.saveImageAsBitmap()
    }

    override fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener) {
        mOnPhotoEditorListener = onPhotoEditorListener
        mGraphicManager.onPhotoEditorListener = mOnPhotoEditorListener
        mBrushDrawingStateListener.setOnPhotoEditorListener(mOnPhotoEditorListener)
    }

    override val isCacheEmpty: Boolean
        get() = viewState.addedViewsCount == 0 && viewState.redoViewsCount == 0

    // region Shape
    override fun setShape(shapeBuilder: ShapeBuilder) {
        drawingView?.currentShapeBuilder = shapeBuilder
    } // endregion

    companion object {
        private const val TAG = "PhotoEditor"
    }

    init {
        drawingView?.drawingGraphicalElement = this@PhotoEditorImpl.drawingGraphicalElement
        drawingView?.setBrushViewChangeListener(mBrushDrawingStateListener)
        val mDetector = GestureDetector(
            context,
            PhotoEditorImageViewListener(
                viewState,
                object : OnSingleTapUpCallback {
                    override fun onSingleTapUp() {
                        //clearHelperBox()
                    }
                }
            )
        )
        imageView?.setOnTouchListener { _, event ->
            mOnPhotoEditorListener?.onTouchSourceImage(event)
            mDetector.onTouchEvent(event)
        }
        photoEditorView.setClipSourceImage(builder.clipSourceImage)
    }
}