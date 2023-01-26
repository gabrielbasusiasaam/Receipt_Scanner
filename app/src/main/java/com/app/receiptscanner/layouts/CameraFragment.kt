@file:Suppress("DEPRECATION")

package com.app.receiptscanner.layouts

import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentCameraBinding
import com.app.receiptscanner.parser.Parser
import com.app.receiptscanner.parser.TokenRelation.Companion.LINE_CURRENT
import com.app.receiptscanner.parser.TokenRelation.TokenRelationsBuilder
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory
import com.app.receiptscanner.views.CameraView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private var camera: Camera? = null
    private var cameraView: CameraView? = null
    private var cameraLocked = false
    private val receiptViewmodel: ReceiptViewmodel by activityViewModels {
        ReceiptViewmodelFactory(
            application.userRepository,
            application.receiptRepository,
            application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        if (!checkHasCamera()) {
            Toast.makeText(
                activity,
                "Device does not have a rear facing camera",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
        }
        val recogniser = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val pictureCallback = Camera.PictureCallback { bytes, _ ->
            lifecycleScope.launch(Dispatchers.IO) {
                if (bytes == null) {
                    camera?.stopPreview()
                    camera?.startPreview()
                    return@launch
                }
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val image = InputImage.fromBitmap(bitmap, getCameraRotation())
                recogniser.process(image).addOnSuccessListener {
                    Log.e("TR", "SUCCESS - ${it.textBlocks.size}")
                    cameraLocked = false
                    val tokenRelations = TokenRelationsBuilder()
                        .addTokenRelation("Items", 1, LINE_CURRENT)
                        .addTokenRelation("MID", 1, LINE_CURRENT)
                        .addTokenRelation("Pay", 1, LINE_CURRENT)
                        .addTokenRelation("CARD", 1, LINE_CURRENT)
                        .build()
                    val parser = Parser(tokenRelations)
                    val tree = parser.createSyntaxTree(parser.tokenize(it))
                    tree.childNodes.forEach { node ->
                        node.childNodes.firstOrNull()?.let { value ->
                            receiptViewmodel.setField(node.content, value.content)
                        }
                    }
                    findNavController().navigate(R.id.action_cameraFragment_to_receiptFragment)
                }.addOnFailureListener {
                    cameraLocked = false
                    Log.e("TR", "FAILED - ${it.message}")
                }
                Log.e("TR", "GOT HERE!")
                camera?.stopPreview()
                camera?.startPreview()
            }

        }

        binding.captureButton.setOnClickListener {
            if (!cameraLocked) {
                camera?.takePicture(null, null, pictureCallback)
                cameraLocked = true
            } else {
                Toast.makeText(
                    activity,
                    "Please wait for the image to be processed!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let {
            it.hide(WindowInsetsCompat.Type.navigationBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        camera = getCamera()
        val parameters = camera?.parameters
        parameters?.flashMode = Camera.Parameters.FOCUS_MODE_AUTO
        camera?.parameters = parameters
        camera?.startPreview()

        binding.frameLayout.removeAllViews()

        cameraView = camera?.let {
            CameraView(activity, it)
        }
        camera?.parameters
        cameraView?.also { binding.frameLayout.addView(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        camera?.release()
        camera = null
    }

    override fun onStop() {
        super.onStop()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(
            WindowInsetsCompat.Type.navigationBars()
        )
    }


    private fun checkHasCamera(): Boolean {
        val cameraCount = Camera.getNumberOfCameras()
        for (cameraId in 0 until cameraCount) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return true
        }
        return false
    }

    private fun getCamera(): Camera? {
        return try {
            Camera.open()
        } catch (e: Exception) {
            null
        }
    }

    //FROM https://developers.google.com/ml-kit/vision/text-recognition/android
    private fun getCameraRotation(): Int {
        val rotation = when (activity.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        return when (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            false -> {
                val orientation = (info.orientation + rotation) % 360
                (360 - orientation) % 360
            }
            true -> {
                (info.orientation - rotation + 360) % 360
            }
        }
    }
}