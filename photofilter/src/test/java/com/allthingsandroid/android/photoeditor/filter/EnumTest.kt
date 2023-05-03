package com.allthingsandroid.android.photoeditor.filter

import com.allthingsandroid.android.photoeditor.filter.PhotoFilter
import junit.framework.TestCase.assertEquals
import org.junit.Test

class EnumTest {

    @Test
    fun testNumberOfPhotoFilterTypes() {
        assertEquals(com.allthingsandroid.android.photoeditor.filter.PhotoFilter.values().size.toLong(), 24)
    }
}