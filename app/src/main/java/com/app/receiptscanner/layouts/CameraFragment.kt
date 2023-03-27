@file:Suppress("DEPRECATION")

package com.app.receiptscanner.layouts

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentCameraBinding
import com.app.receiptscanner.parser.FieldTemplate.MARKS_AND_SPENCERS_ID
import com.app.receiptscanner.parser.Parser
import com.app.receiptscanner.parser.TokenField.Companion.CHECK_AFTER
import com.app.receiptscanner.parser.TokenField.TokenFieldBuilder
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@Suppress("UNUSED_VARIABLE", "unused", "UNCHECKED_CAST")
class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private var imageCapture: ImageCapture? = null
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
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it) findNavController().popBackStack()
            }

        val isPermitted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA,
        )
        if (isPermitted != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.captureButton.setOnClickListener {
            if (!cameraLocked) {
                takePicture()
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
        startCamera()
    }


    override fun onPause() {
        super.onPause()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onStop() {
        super.onStop()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.surfaceView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("Camera", "Failed", e)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun takePicture() {
        val imageCapture = imageCapture ?: return
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val recogniser =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                image.image?.let { rawImage ->
                    val inputImage =
                        InputImage.fromMediaImage(rawImage, image.imageInfo.rotationDegrees)
                    recogniser.process(inputImage).addOnSuccessListener {
                        Log.e("TR", "SUCCESS - ${it.textBlocks.size}")
                        cameraLocked = false
                        val tokenRelations = TokenFieldBuilder()
                            .addKeyWordRelation("Items", 1, CHECK_AFTER)
                            .addKeyWordRelation(arrayListOf("BALANCE", "TO", "PAY"), 1, CHECK_AFTER)
                            .build()

                        val parser = Parser(tokenRelations)
                        val tokens = parser.tokenize(it)
                        val relations = parser.generateRelations(tokens)
                        val syntaxTree = parser.createSyntaxTree(relations)
                        val receipt = parser.createReceipt(syntaxTree, MARKS_AND_SPENCERS_ID)
                        if (receipt == null) {
                            Toast.makeText(
                                activity,
                                "Failed to create receipt! Please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }
                        receiptViewmodel.setNormalizedReceipt(receipt)
                        findNavController().navigate(R.id.action_cameraFragment_to_receiptFragment)
                    }.addOnFailureListener {
                        cameraLocked = false
                        Log.e("TR", "FAILED - ${it.message}")
                    }
                }
            }
        }
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity), callback)
    }
}