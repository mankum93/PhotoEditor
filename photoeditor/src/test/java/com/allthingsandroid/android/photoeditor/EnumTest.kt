package com.allthingsandroid.android.photoeditor

import junit.framework.TestCase.assertEquals
import org.junit.Test

class EnumTest {
    @Test
    fun testNumberOfViewTypes() {
        assertEquals(ViewType.values().size.toLong(), 4)
    }
}