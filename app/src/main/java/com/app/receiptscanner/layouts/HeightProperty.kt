package com.app.receiptscanner.layouts

import android.util.Property
import android.view.View

class HeightProperty : Property<View, Float>(Float::class.java, "height") {
    override fun get(`object`: View?) = `object`?.layoutParams?.height?.toFloat()

    override fun set(view: View, value: Float?) {
        value?.let {
            view.layoutParams.height = it.toInt()
            view.layoutParams = view.layoutParams
        }
    }
}