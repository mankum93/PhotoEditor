package com.allthingsandroid.android.photoediting

import android.app.Application
import android.graphics.Typeface
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class EditImageScreenViewModel(application: Application) : AndroidViewModel(application) {

    val mWonderFont = Typeface.createFromAsset(application.assets, "beyond_wonderland.ttf")

    var mIsFilterVisible = false

    // Source image for the image editor
    var currentSourceUri: Uri? = null

    // File where we want the Camera picked image output
    var cameraCapturedImageOutFileTemp: File? = null
    // File where we want the Camera picked image output
    var cameraCapturedImageOutFile: File? = null

    // It is possible that the Camera/Gallery Activity ends up rotating
    // to the orientation different than the orientation of this Activity
    // and coming back to this Activity then would change the orientation
    // of this Activity
    // Lets track if we requested a pick
    var pickImageRequested: Boolean = false
    // Lifecycle of this variable
    // Starts when the Camera/Gallery returns the picked image
    var pickedImageUri: Uri? = null

    fun deleteCurrentPickedImageFromCamera(){
        val file = cameraCapturedImageOutFile
        GlobalScope.launch(Dispatchers.IO){
            file?.delete()
        }
    }

    override fun onCleared() {
        deleteCurrentPickedImageFromCamera()

        super.onCleared()
    }
}