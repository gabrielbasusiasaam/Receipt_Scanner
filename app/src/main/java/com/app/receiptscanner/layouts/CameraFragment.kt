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
import com.app.receiptscanner.parser.FieldTemplate
import com.app.receiptscanner.parser.Parser
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

        // Boolean signifying if the user already allows the app to use the camera
        val isPermitted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA,
        )

        // If the camera is not already allowed, the app requests the user for permission,
        // returning to the previous screen if this permission is denied
        if (isPermitted != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // If a picture is already being processed, the app shows the user a popup telling them to
        // wait, otherwise a picture is taken. This is to avoid multiple calls to the parser at
        // once, which would have undefined behaviour
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

    /**
     * When the fragment is visable to the user again, the camera is started and
     * the screen is put back in fullscreen mode
     */
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
            // Sets up the Surface View to show the user what their camera sees
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.surfaceView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            imageCapture = ImageCapture.Builder().build()

            // Tries to attach the camera to the Fragments lifecycle
            // If, for whatever reason, this fails it navigates back to the previous screen, as
            // the use of the camera is necessary here
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
                findNavController().popBackStack()
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun takePicture() {
        // Makes sure that the camera is set up to allow for a picture to be taken, returning early
        // if it not properly set up
        val imageCapture = imageCapture ?: return
        // This is explicitly run after the image has been captured, and only in the case that it is
        // captured successfully
        val callback = object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val recogniser =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                image.image?.let { rawImage ->
                    val inputImage =
                        InputImage.fromMediaImage(rawImage, image.imageInfo.rotationDegrees)
                    recogniser.process(inputImage).addOnSuccessListener {
                        // Unlocks the camera once the image has been captured
                        cameraLocked = false

                        // Gets the fields for the receipt type selected in the
                        // ReceiptCreationFragment
                        val receiptType = receiptViewmodel.getReceipt().type
                        val fields = FieldTemplate.getFieldsById(receiptType)

                        // Extracts the required information from the receipt, and creates a
                        // normalized receipt from the result
                        // These could be merged into one method, however, I have kept them separate
                        // to make the flow between the methods explicit
                        val parser = Parser(fields)
                        val tokens = parser.tokenize(it)
                        val relations = parser.generateRelations(tokens)
                        val syntaxTree = parser.createSyntaxTree(relations)
                        val receipt = parser.createReceipt(syntaxTree, receiptType)

                        // If it fails to create a receipt, the user is shown an error and allowed
                        // to try again
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
                        // Unlocks the camera, and allows the user to retake a picture
                        cameraLocked = false
                        Toast.makeText(
                            activity,
                            "Text Extraction Failed! Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        imageCapture.takePicture(ContextCompat.getMainExecutor(activity), callback)
    }
}