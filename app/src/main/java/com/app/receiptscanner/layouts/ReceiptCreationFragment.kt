package com.app.receiptscanner.layouts

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentReceiptCreationBinding
import com.app.receiptscanner.parser.FieldTemplate.LIDL_ID
import com.app.receiptscanner.parser.FieldTemplate.MARKS_AND_SPENCERS_ID
import com.app.receiptscanner.parser.FieldTemplate.MORRISONS_ID
import com.app.receiptscanner.parser.FieldTemplate.WAITROSE_ID
import com.app.receiptscanner.parser.FieldTemplate.getFieldsById
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory

class ReceiptCreationFragment : Fragment() {
    private var _binding: FragmentReceiptCreationBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val animationDuration = 1000L
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
        _binding = FragmentReceiptCreationBinding.inflate(inflater, container, false)

        // Sets up the Provider Selection Spinner / Dropdown menu
        val items = arrayOf("Marks and Spencers", "Lidl", "Morrisons", "Sainsbury's")
        val arrayAdapter = ArrayAdapter(requireActivity(), R.layout.list_item, items)
        (binding.receiptProviderSelection.editText as? AutoCompleteTextView)?.let {
            it.setAdapter(arrayAdapter)
            it.setText(items[0], false)
        }
        val logos = arrayOf(
            R.mipmap.ms_foreground,
            R.mipmap.lidl_logo_foreground,
            R.mipmap.morrisons_logo_foreground,
            R.mipmap.sainsbury_logo_foreground
        )

        binding.imageView2.setImageResource(logos[0])
        // Changes the image when the user selects a provider
        binding.receiptProviderSelection.editText?.doAfterTextChanged {
            val index = items.indexOf(it.toString()).coerceAtMost(logos.lastIndex)
            binding.imageView2.setImageResource(logos[index])
        }
        // If the user chooses to create manually, they are straight to a screen with a clear
        // receipt to edit.
        binding.createManuallyButton.setOnClickListener {
            val text = binding.receiptProviderSelection.editText?.text ?: return@setOnClickListener
            val index = items.indexOf(text.toString()).coerceAtMost(logos.lastIndex)
            setDefaultReceipt(index)
            findNavController().navigate(R.id.action_receiptCreationFragment_to_receiptFragment)
        }

        // Otherwise they are sent to a screen which allows them to take a photo
        binding.createFromCameraButton.setOnClickListener {
            val text = binding.receiptProviderSelection.editText?.text ?: return@setOnClickListener
            val index = items.indexOf(text.toString()).coerceAtMost(logos.lastIndex)
            setDefaultReceipt(index)
            findNavController().navigate(R.id.action_receiptCreationFragment_to_cameraFragment)
        }

        // This is only run after the cardView has been visually laid out, and thus has a measured
        // height. Two different animation sets are made, one for expanding the card, and one for
        // shrinking the card. These animations alternate, and are activated on press/click
        binding.cardView.post {
            val startHeight = binding.cardView.height.toFloat()
            val endHeight = startHeight * 0.5f

            // These control whether the animation is already playing, and therefore the user
            // pressing the card should be ignored, and which animation to play respectively
            var isAnimating = false
            var isExpanded = false

            val expansionAnimation =
                ObjectAnimator.ofFloat(binding.cardView, HeightProperty(), startHeight, endHeight)
            val shrinkAnimation =
                ObjectAnimator.ofFloat(binding.cardView, HeightProperty(), endHeight, startHeight)

            // The following have an issue in their duration. For some devices the animation
            // takes the set duration, whereas for others it takes half as long.
            // Unsure how to fix this
            val expansionAnimatorSet = AnimatorSet().apply {
                play(expansionAnimation)
                duration = animationDuration
                interpolator = AccelerateDecelerateInterpolator()

                // At the start of the animation we change the scale type as otherwise the final
                // image won't match up with the size of the view
                // This unintentionally creates a little inflation animation, which is unavoidable
                doOnStart {
                    binding.imageView2.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                doOnEnd {
                    isAnimating = false
                }
            }
            val shrinkAnimatorSet = AnimatorSet().apply {
                play(shrinkAnimation)
                duration = animationDuration
                interpolator = AccelerateDecelerateInterpolator()

                // Once it has finished animating the scale type is changed. This centers the logo
                doOnEnd {
                    binding.imageView2.scaleType = ImageView.ScaleType.FIT_XY
                    isAnimating = false
                }
            }


            // When the user presses the image, an animation plays either expanding or shrinking
            // the image
            binding.cardView.isClickable = true
            binding.cardView.isFocusable = true
            binding.cardView.setOnClickListener {
                // Animating is used here to make sure that the animation cannot be started whilst
                // it is already playing
                if (!isAnimating) {
                    isAnimating = true
                    when (isExpanded) {
                        false -> expansionAnimatorSet.start()
                        else -> shrinkAnimatorSet.start()
                    }
                    isExpanded = !isExpanded
                }
            }
        }

        return binding.root
    }

    private fun setDefaultReceipt(index: Int) {
        // Come back and replace WAITROSE_ID with SAINSBURYS_ID
        val ids = arrayOf(MARKS_AND_SPENCERS_ID, LIDL_ID, MORRISONS_ID, WAITROSE_ID)
        val fields = getFieldsById(ids[index])
        val template = NormalizedReceipt(-1, "", -1, "", ids[index], fields)
        receiptViewmodel.setNormalizedReceipt(template)
    }
}