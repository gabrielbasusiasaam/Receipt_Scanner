package com.app.receiptscanner.views

import android.annotation.SuppressLint
import android.app.Activity
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

@SuppressLint("ViewConstructor")
class CameraView(private val activity: Activity, private val camera: Camera) : SurfaceView(activity), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraView)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        camera.apply {
            try{
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: IOException) {
                Log.e("Camera", "Error: ${e.message}")
            }
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        if(mHolder.surface == null) return

        try {
            camera.stopPreview()
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
            val degrees = when(activity.windowManager.defaultDisplay.rotation) {
                Surface.ROTATION_0 -> 0
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }

            val result = when(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                false -> {
                    val orientation = (info.orientation + degrees) % 360
                    (360 - orientation) % 360
                }
                true -> {
                    (info.orientation - degrees + 360) % 360
                }
            }
            camera.setDisplayOrientation(result)
        } catch (_: Exception) {}

        camera.apply {
            try {
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e: Exception) {
                Log.e("Camera", "Error: ${e.message}")
            }
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {}
}