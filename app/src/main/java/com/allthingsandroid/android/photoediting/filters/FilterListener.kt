package com.allthingsandroid.android.photoediting.filters

import com.allthingsandroid.android.photoeditor.filter.PhotoFilter

interface FilterListener {
    fun onFilterSelected(photoFilter: com.allthingsandroid.android.photoeditor.filter.PhotoFilter)
}