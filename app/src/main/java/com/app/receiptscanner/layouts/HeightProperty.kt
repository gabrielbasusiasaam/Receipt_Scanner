package com.app.receiptscanner.layouts

import android.util.Property
import android.view.View

/**
 * A class representing the 'height' property of a view. This is used during animations to change
 * the view's height.
 */
class HeightProperty : Property<View, Float>(Float::class.java, "height") {
    override fun get(view: View?) = view?.layoutParams?.height?.toFloat()

    override fun set(view: View, value: Float?) {
        value?.let {
            view.layoutParams.height = it.toInt()
            view.layoutParams = view.layoutParams
        }
    }
}