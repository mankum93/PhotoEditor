package com.allthingsandroid.android.photoediting.util

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import androidx.annotation.AnyRes
import androidx.exifinterface.media.ExifInterface


fun getExifOrientation(context: Context, imageFile: Uri): Int {
    val ei = context.contentResolver.openInputStream(imageFile)?.use { ExifInterface(it) }
    val orientation = ei?.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_UNDEFINED
    )
    return orientation ?: ExifInterface.ORIENTATION_UNDEFINED
}

@Throws(Resources.NotFoundException::class)
fun getUriForResource(context: Context, @AnyRes resId: Int
): Uri {
    val res: Resources = context.resources
    return Uri.parse(
        ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId)
    )
}

fun isSdkHigherThan29(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}