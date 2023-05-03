package com.allthingsandroid.android.photoediting.util

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.getExifOrientationDegrees
import java.security.MessageDigest


class MyRotationTransformation(private val orientation: Int): BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("rotation correction transformation".toByte());
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val exifOrientationDegrees = getExifOrientationDegrees(orientation)
        return TransformationUtils.rotateImageExif(pool, toTransform, exifOrientationDegrees)
    }
}