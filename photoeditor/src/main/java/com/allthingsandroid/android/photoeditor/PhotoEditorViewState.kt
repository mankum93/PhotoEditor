package com.allthingsandroid.android.photoeditor

import java.util.*

/**
 * Tracked state of user-added views (stickers, emoji, text, etc)
 */
class PhotoEditorViewState {
    var currentSelectedView: GraphicalBase? = null
    private val addedViews: MutableList<GraphicalBase>
    private val redoViews: Stack<GraphicalBase>
    fun clearCurrentSelectedView() {
        currentSelectedView = null
    }

    fun getAddedView(index: Int): GraphicalBase {
        return addedViews[index]
    }

    val addedViewsCount: Int
        get() = addedViews.size

    fun clearAddedViews() {
        addedViews.clear()
    }

    fun addAddedView(view: GraphicalBase) {
        addedViews.add(view)
    }

    fun removeAddedView(view: GraphicalBase) {
        addedViews.remove(view)
    }

    fun removeAddedView(index: Int): GraphicalBase {
        return addedViews.removeAt(index)
    }

    fun containsAddedView(view: GraphicalBase): Boolean {
        return addedViews.contains(view)
    }

    /**
     * Replaces a view in the current "added views" list.
     *
     * @param view The view to replace
     * @return true if the view was found and replaced, false if the view was not found
     */
    fun replaceAddedView(view: GraphicalBase): Boolean {
        val i = addedViews.indexOf(view)
        if (i > -1) {
            addedViews[i] = view
            return true
        }
        return false
    }

    fun clearRedoViews() {
        redoViews.clear()
    }

    fun pushRedoView(view: GraphicalBase) {
        redoViews.push(view)
    }

    fun popRedoView(): GraphicalBase {
        return redoViews.pop()
    }

    val redoViewsCount: Int
        get() = redoViews.size

    fun getRedoView(index: Int): GraphicalBase {
        return redoViews[index]
    }

    init {
        addedViews = ArrayList()
        redoViews = Stack()
    }
}