package com.allthingsandroid.android.photoediting

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.allthingsandroid.android.photoeditor.DragToDeleteTouchListener
import com.allthingsandroid.android.photoeditor.EmojiGraphicalElementBuilder
import com.allthingsandroid.android.photoeditor.OnPhotoEditorListener
import com.allthingsandroid.android.photoeditor.PhotoEditor
import com.allthingsandroid.android.photoeditor.SaveFileResult
import com.allthingsandroid.android.photoeditor.SaveSettings
import com.allthingsandroid.android.photoeditor.StickerGraphicalElementBuilder
import com.allthingsandroid.android.photoeditor.TextGraphicalElementBuilder
import com.allthingsandroid.android.photoeditor.TextStyleBuilder
import com.allthingsandroid.android.photoeditor.ViewType
import com.allthingsandroid.android.photoeditor.defaultTouchBehaviors
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.allthingsandroid.android.photoediting.EmojiBSFragment.EmojiListener
import com.allthingsandroid.android.photoediting.StickerBSFragment.StickerListener
import com.allthingsandroid.android.photoediting.base.BaseActivity
import com.allthingsandroid.android.photoediting.databinding.ActivityEditImageBinding
import com.allthingsandroid.android.photoediting.databinding.DragToDeleteBinding
import com.allthingsandroid.android.photoediting.filters.FilterListener
import com.allthingsandroid.android.photoediting.filters.FilterViewAdapter
import com.allthingsandroid.android.photoediting.tools.EditingToolsAdapter
import com.allthingsandroid.android.photoediting.tools.EditingToolsAdapter.OnItemSelected
import com.allthingsandroid.android.photoediting.tools.ToolType
import com.allthingsandroid.android.photoediting.util.getUriForResource
import com.crazylegend.view.dp
import com.crazylegend.view.dpToPx
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.allthingsandroid.android.photoeditor.*
import com.allthingsandroid.android.photoeditor.shape.ShapeBuilder
import com.allthingsandroid.android.photoeditor.shape.ShapeType
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*


class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties, EmojiListener, StickerListener,
    OnItemSelected, FilterListener {

    private val viewModel: EditImageScreenViewModel by viewModels()

    lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPropertiesBSFragment: PropertiesBSFragment
    private lateinit var mShapeBSFragment: ShapeBSFragment
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mEmojiBSFragment: EmojiBSFragment
    private lateinit var mStickerBSFragment: StickerBSFragment

    private val mEditingToolsAdapter = EditingToolsAdapter(this)
    private val mFilterViewAdapter = FilterViewAdapter(this)
    private val mConstraintSet = ConstraintSet()

    @VisibleForTesting
    var mSaveImageUri: Uri? = null

    private lateinit var mSaveFileHelper: FileSaveHelper

    private var _binding: ActivityEditImageBinding? = null
    private val binding: ActivityEditImageBinding get() = _binding!!

    private var _bindingDragToDelete: DragToDeleteBinding? = null
    private val bindingDragToDelete: DragToDeleteBinding get() = _bindingDragToDelete!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()

        _binding = ActivityEditImageBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        initViews()

        _bindingDragToDelete = DragToDeleteBinding.inflate(LayoutInflater.from(this))
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.photoEditorView.addView(bindingDragToDelete.root, layoutParams)
        // Initial visibility
        bindingDragToDelete.actionDragToDelete.visibility = View.GONE

        //mPhotoEditorView.source.adjustViewBounds = false

        handleIntentImage()

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mStickerBSFragment.setStickerListener(this)
        mEmojiBSFragment.setEmojiListener(this)
        mPropertiesBSFragment.setPropertiesChangeListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvConstraintTools.layoutManager = llmTools
        binding.rvConstraintTools.adapter = mEditingToolsAdapter

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilterView.layoutManager = llmFilters
        binding.rvFilterView.adapter = mFilterViewAdapter

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this)

        // It is possible that the Camera/Gallery Activity ends up rotating
        // to the orientation different than the orientation of this Activity
        // and coming back to this Activity then would change the orientation
        // of this Activity
        // Lets track if we requested a pick
        if (!viewModel.pickImageRequested) {
            // Normal flow
            if (viewModel.currentSourceUri == null) {
                viewModel.currentSourceUri =
                    getUriForResource(applicationContext, R.drawable.paris_tower)
            }
            //mPhotoEditorView.source.setImageURI(viewModel.currentSourceUri)
            Glide.with(this@EditImageActivity)
                .load(viewModel.currentSourceUri)
                .fitCenter()
                //.transform(MyRotationTransformation(getExifOrientation(applicationContext, cameraPickedImageUri)))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.photoEditorView.source)
        }

        mSaveFileHelper = FileSaveHelper(this)
    }

    private fun handleIntentImage() {
        if (intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_EDIT, ACTION_NEXTGEN_EDIT -> {
                try {
                    val uri = intent.data
                    viewModel.currentSourceUri = uri
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            else -> {
                val intentType = intent.type
                if (intentType != null && intentType.startsWith("image/")) {
                    val imageUri = intent.data
                    if (imageUri != null) {
                        viewModel.currentSourceUri = imageUri
                    }
                }
            }
        }
    }

    private fun initViews() {

        binding.imgUndo.setOnClickListener(this)

        binding.imgRedo.setOnClickListener(this)

        binding.imgCamera.setOnClickListener(this)

        binding.imgGallery.setOnClickListener(this)

        binding.imgSave.setOnClickListener(this)

        binding.imgClose.setOnClickListener(this)

        binding.imgShare.setOnClickListener(this)
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment =
            TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditorListener {
            override fun onDone(inputText: String?, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                if (rootView != null) {
                    // TODO: Uncomment and fix
                    //mPhotoEditor.editText(rootView, inputText, styleBuilder)
                }
                binding.txtCurrentTool.setText(R.string.label_text)
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onTouchSourceImage(event: MotionEvent?) {
        Log.d(TAG, "onTouchView() called with: event = [$event]")
    }

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor.undo()
            R.id.imgRedo -> mPhotoEditor.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.imgShare -> shareImage()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                viewModel.cameraCapturedImageOutFileTemp = File(cacheDir, "${UUID.randomUUID()}.jpg")
                val cameraOutUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    viewModel.cameraCapturedImageOutFileTemp!!
                )
                cameraIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT, cameraOutUri
                )
                viewModel.pickImageRequested = true
                startActivityForResult(Intent.createChooser(cameraIntent, "Select Camera App"), CAMERA_REQUEST)
            }
            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                viewModel.pickImageRequested = true
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }

    private fun shareImage() {
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(R.string.msg_save_image_to_share))
            return
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(saveImageUri))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    private fun buildFileProviderUri(uri: Uri): Uri {
        if (FileSaveHelper.isSdkHigherThan28()) {
            return uri
        }
        val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

        return FileProvider.getUriForFile(
            this,
            applicationContext.packageName,
            File(path)
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    lifecycleScope.launch {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder()
                                .setClearViewsEnabled(true)
                                .setTransparencyEnabled(true)
                                .build()

                            val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                            if (result is SaveFileResult.Success) {
                                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                hideLoading()
                                showSnackbar("Image Saved Successfully")
                                mSaveImageUri = uri
                                binding.photoEditorView.source.setImageURI(mSaveImageUri)
                            } else {
                                hideLoading()
                                Log.d("EditImageActivity", "Failed to save Image", (result as SaveFileResult.Failure).exception)
                                showSnackbar("Failed to save Image")
                            }
                        } else {
                            hideLoading()
                            error?.let { showSnackbar(error) }
                        }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // TODO(lucianocheng): Replace onActivityResult with Result API from Google
    //                     See https://developer.android.com/training/basics/intents/result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    viewModel.pickImageRequested = false
                    mPhotoEditor.clearAllViews()
                    lifecycleScope.launch(){
                        // Now that we are going to apply this picked image from camera,
                        // schedule delete of any last picked image from Camera
                        viewModel.deleteCurrentPickedImageFromCamera()
                        // temp's purpose ends here
                        viewModel.cameraCapturedImageOutFile = viewModel.cameraCapturedImageOutFileTemp
                        viewModel.cameraCapturedImageOutFileTemp = null

                        viewModel.pickedImageUri = FileProvider.getUriForFile(
                            applicationContext,
                            applicationContext.packageName + ".provider",
                            viewModel.cameraCapturedImageOutFile!!
                        )
                        viewModel.currentSourceUri = viewModel.pickedImageUri
                        Glide.with(this@EditImageActivity)
                            .load(viewModel.currentSourceUri)
                            .fitCenter()
                            //.transform(MyRotationTransformation(getExifOrientation(applicationContext, cameraPickedImageUri)))
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    lifecycleScope.launch(){
                                        //viewModel.clearPickedImage()
                                    }
                                    return false
                                }
                            })
                            .into(binding.photoEditorView.source)
                    }
                }
                PICK_REQUEST -> try {
                    viewModel.pickImageRequested = false
                    mPhotoEditor.clearAllViews()
                    lifecycleScope.launch(){
                        viewModel.pickedImageUri = data?.data
                        if(viewModel.pickedImageUri != null) {
                            viewModel.currentSourceUri = viewModel.pickedImageUri
                            binding.photoEditorView.source.setImageURI(viewModel.currentSourceUri)
                            //mPhotoEditorView.source.setImageDrawable(viewModel.currentSourceDrawable)

                            /*GlideApp.with(this@EditImageActivity)
                                .load(viewModel.currentSourceUri)
                                .optionalCenterInside()
                                //.transform(MyRotationTransformation(getExifOrientation(applicationContext, cameraPickedImageUri)))
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable?,
                                        model: Any?,
                                        target: Target<Drawable>?,
                                        dataSource: DataSource?,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        lifecycleScope.launch(){
                                            viewModel.clearPickedImage()
                                        }
                                        return false
                                    }
                                })
                                .into(mPhotoEditorView.source)*/
                        }
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
        binding.txtCurrentTool.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
        binding.txtCurrentTool.setText(R.string.label_brush)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
        binding.txtCurrentTool.setText(R.string.label_brush)
    }

    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        val emoji = mPhotoEditor.addEmoji(EmojiGraphicalElementBuilder())
        emoji.contentView.text = emojiUnicode
        binding.txtCurrentTool.setText(R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        val sticker = mPhotoEditor.addImage(
            StickerGraphicalElementBuilder()
                .viewPlacement(
                    ViewPlacement.LayoutParamsBased(
                        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                                addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                        }
                    )
                )
                .touchHandlers(mPhotoEditor.defaultTouchBehaviors().apply {
                    add(DefaultDragToDeleteTouchListener(bindingDragToDelete.actionDragToDelete, object:
                        DragToDeleteTouchListener.EventsListener{
                        override fun onTouchIntersectDeleteView(
                            tag: String,
                            view: View,
                            positionRelToDeleteView: DragToDeleteTouchListener.PositionRelToDeleteView
                        ) {
                            if(positionRelToDeleteView == DragToDeleteTouchListener.PositionRelToDeleteView.ENTERING_DELETE_VIEW){
                                vibrationEffectOnEnterDeleteViewArea()
                            }
                        }

                        override fun onTouchEndOverDeleteView(tag: String, view: View) {
                            mPhotoEditor.removeGraphicalElement(tag)
                        }
                    }))
                })
        )
        /*sticker.rootView.apply {
            x = (binding.photoEditorView.right - 32.dp - this.measuredWidth).toFloat()
            y = (binding.photoEditorView.bottom - 32.dp - this.measuredHeight).toFloat()
        }*/
        sticker.contentView.setImageBitmap(bitmap)
        binding.txtCurrentTool.setText(R.string.label_sticker)
    }

    private fun vibrationEffectOnEnterDeleteViewArea() {
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if(vibratorService.hasVibrator()){
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibratorService.vibrate(VibrationEffect.createOneShot(
                    500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26
                vibratorService.vibrate(500)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    override fun onFilterSelected(photoFilter: com.allthingsandroid.android.photoeditor.filter.PhotoFilter) {
        //binding.photoEditorView.setFilterEffect(photoFilter)
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            ToolType.SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
                binding.txtCurrentTool.setText(R.string.label_shape)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        val text = mPhotoEditor.addText(TextGraphicalElementBuilder())
                        text.contentView.text = inputText
                        text.contentView.setTextColor(colorCode)
                        binding.txtCurrentTool.setText(R.string.label_text)
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor.brushEraser()
                binding.txtCurrentTool.setText(R.string.label_eraser_mode)
            }
            ToolType.FILTER -> {
                binding.txtCurrentTool.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
            ToolType.STICKER -> showBottomSheetDialogFragment(mStickerBSFragment)
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    private fun showFilter(isVisible: Boolean) {
        viewModel.mIsFilterVisible = isVisible
        mConstraintSet.clone(binding.root)

        val rvFilterId: Int = binding.rvFilterView.id

        if (isVisible) {
            mConstraintSet.clear(rvFilterId, ConstraintSet.START)
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(rvFilterId, ConstraintSet.END)
        }

        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(binding.root, changeBounds)

        mConstraintSet.applyTo(binding.root)
    }

    override fun onBackPressed() {
        if (viewModel.mIsFilterVisible) {
            showFilter(false)
            binding.txtCurrentTool.setText(R.string.app_name)
        } else if (!mPhotoEditor.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {

        private const val TAG = "EditImageActivity"

        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
        const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    }
}