package com.allthingsandroid.android.photoeditor

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import com.allthingsandroid.android.photoeditor.shape.ShapeBuilder


interface PhotoEditor {

    fun addImage(stickerBuilder: StickerGraphicalElementBuilder): StickerGraphicalElement

    fun addText(textGraphicalElementBuilder: TextGraphicalElementBuilder): TextGraphicalElement

    fun removeGraphicalElement(tag: String): Boolean

    fun getGraphicalElement(tag: String): GraphicalBase?

    fun addEmoji(emojiGraphicalElementBuilder: EmojiGraphicalElementBuilder): EmojiGraphicalElement

    /**
     * Enable/Disable drawing mode to draw on [PhotoEditorView]
     *
     * @param brushDrawingMode true if mode is enabled
     */
    fun setBrushDrawingMode(brushDrawingMode: Boolean)

    /**
     * @return true is brush mode is enabled
     */
    val brushDrawableMode: Boolean?

    /**
     * set opacity/transparency of brush while painting on [DrawingView]
     * @param opacity opacity is in form of percentage
     */
    @Deprecated(
        """use {@code setShape} of a ShapeBuilder
     
      """
    )
    fun setOpacity(@IntRange(from = 0, to = 100) opacity: Int)

    /**
     * set the eraser size
     * **Note :** Eraser size is different from the normal brush size
     *
     * @param brushEraserSize size of eraser
     */
    fun setBrushEraserSize(brushEraserSize: Float)

    /**
     * @return provide the size of eraser
     * @see PhotoEditor.setBrushEraserSize
     */
    val eraserSize: Float
    /**
     * @return provide the size of eraser
     * @see PhotoEditor.setBrushSize
     */
    /**
     * Set the size of brush user want to paint on canvas i.e [DrawingView]
     * @param size size of brush
     */
    @set:Deprecated(
        """use {@code setShape} of a ShapeBuilder
     
      """
    )
    var brushSize: Float
    /**
     * @return provide the size of eraser
     * @see PhotoEditor.setBrushColor
     */
    /**
     * set brush color which user want to paint
     * @param color color value for paint
     */
    @set:Deprecated(
        """use {@code setShape} of a ShapeBuilder
     
      """
    )
    var brushColor: Int

    /**
     *
     *
     * Its enables eraser mode after that whenever user drags on screen this will erase the existing
     * paint
     * <br></br>
     * **Note** : This eraser will work on paint views only
     *
     *
     */
    fun brushEraser()

    /**
     * Undo the last operation perform on the [PhotoEditor]
     *
     * @return true if there nothing more to undo
     */
    fun undo(): Boolean

    /**
     * Redo the last operation perform on the [PhotoEditor]
     *
     * @return true if there nothing more to redo
     */
    fun redo(): Boolean

    /**
     * Removes all the edited operations performed [PhotoEditorView]
     * This will also clear the undo and redo stack
     */
    fun clearAllViews()

    /**
     * Save the edited image on given path
     *
     * @param imagePath      path on which image to be saved
     * @param saveSettings   builder for multiple save options [SaveSettings]
     */
    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    suspend fun saveAsFile(
        imagePath: String,
        saveSettings: SaveSettings = SaveSettings.Builder().build()
    ): SaveFileResult

    /**
     * Save the edited image as bitmap
     *
     * @param saveSettings builder for multiple save options [SaveSettings]
     */
    suspend fun saveAsBitmap(saveSettings: SaveSettings = SaveSettings.Builder().build()): Bitmap

    /**
     * Callback on editing operation perform on [PhotoEditorView]
     *
     * @param onPhotoEditorListener [OnPhotoEditorListener]
     */
    fun setOnPhotoEditorListener(onPhotoEditorListener: OnPhotoEditorListener)

    /**
     * Check if any changes made need to save
     *
     * @return true if nothing is there to change
     */
    val isCacheEmpty: Boolean

    /**
     * Builder pattern to define [PhotoEditor] Instance
     */
    class Builder(var context: Context, var photoEditorView: PhotoEditorView) {
        @JvmField
        var imageView: ImageView

        @JvmField
        var drawingView: DrawingView? = null

        @JvmField
        var emojiTypeface: Typeface? = null

        // By default, pinch-to-scale is enabled for text
        @JvmField
        var isTextPinchScalable = true

        @JvmField
        var clipSourceImage = false

        /**
         * Building a PhotoEditor which requires a Context and PhotoEditorView
         * which we have setup in our xml layout
         *
         * @param context         context
         * @param photoEditorView [PhotoEditorView]
         */
        init {
            imageView = photoEditorView.source
            drawingView = photoEditorView.drawingView
        }

        /**
         * set default font specific to add emojis
         *
         * @param emojiTypeface typeface for custom font
         * @return [Builder] instant to build [PhotoEditor]
         */
        fun setDefaultEmojiTypeface(emojiTypeface: Typeface?): Builder {
            this.emojiTypeface = emojiTypeface
            return this
        }

        /**
         * Set false to disable pinch-to-scale for text inserts.
         * Set to "true" by default.
         *
         * @param isTextPinchScalable flag to make pinch to zoom for text inserts.
         * @return [Builder] instant to build [PhotoEditor]
         */
        fun setPinchTextScalable(isTextPinchScalable: Boolean): Builder {
            this.isTextPinchScalable = isTextPinchScalable
            return this
        }

        /**
         * @return build PhotoEditor instance
         */
        fun build(): PhotoEditor {
            return PhotoEditorImpl(this)
        }

        /**
         * Set true true to clip the drawing brush to the source image.
         *
         * @param clip a boolean to indicate if brush drawing is clipped or not.
         */
        fun setClipSourceImage(clip: Boolean): Builder {
            clipSourceImage = clip
            return this
        }
    }

    /**
     * A callback to save the edited image asynchronously
     */
    interface OnSaveListener {
        /**
         * Call when edited image is saved successfully on given path
         *
         * @param imagePath path on which image is saved
         */
        fun onSuccess(imagePath: String)

        /**
         * Call when failed to saved image on given path
         *
         * @param exception exception thrown while saving image
         */
        fun onFailure(exception: Exception)
    }

    // region Shape
    /**
     * Update the current shape to be drawn,
     * through the use of a ShapeBuilder.
     */
    fun setShape(shapeBuilder: ShapeBuilder) // endregion
}