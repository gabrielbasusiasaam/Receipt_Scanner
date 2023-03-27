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
import com.app.receiptscanner.parser.FieldTemplate.MARKS_AND_SPENCERS_ID
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
    private val receiptViewmodel: ReceiptViewmodel by activityViewModels {
        ReceiptViewmodelFactory(
            application.userRepository,
            application.receiptRepository,
            application
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptCreationBinding.inflate(inflater, container, false)
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
        val navController = findNavController()
        binding.imageView2.setImageResource(logos[0])
        binding.receiptProviderSelection.editText?.doAfterTextChanged {
            val index = items.indexOf(it.toString()).coerceAtMost(logos.lastIndex)
            binding.imageView2.setImageResource(logos[index])
        }
        binding.createManuallyButton.setOnClickListener {
            val fields = getFieldsById(MARKS_AND_SPENCERS_ID)
            val template = NormalizedReceipt("", -1, "", MARKS_AND_SPENCERS_ID, fields)
            receiptViewmodel.setNormalizedReceipt(template)
            navController.navigate(R.id.action_receiptCreationFragment_to_receiptFragment)
        }

        binding.createFromCameraButton.setOnClickListener {
            val fields = getFieldsById(MARKS_AND_SPENCERS_ID)
            val template = NormalizedReceipt("", -1, "", MARKS_AND_SPENCERS_ID, fields)
            receiptViewmodel.setNormalizedReceipt(template)
            navController.navigate(R.id.action_receiptCreationFragment_to_cameraFragment)
        }

        binding.cardView.post {
            val startHeight = binding.cardView.height.toFloat()
            val endHeight = startHeight * 0.5f
            var animating = false
            var expanded = false
            val expansionAnimation =
                ObjectAnimator.ofFloat(binding.cardView, HeightProperty(), startHeight, endHeight)
            val shrinkAnimation =
                ObjectAnimator.ofFloat(binding.cardView, HeightProperty(), endHeight, startHeight)
            val expansionAnimatorSet = AnimatorSet().apply {
                play(expansionAnimation)
                duration = 1000L
                interpolator = AccelerateDecelerateInterpolator()
                doOnStart {
                    binding.imageView2.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                doOnEnd {
                    animating = false
                }
            }
            val shrinkAnimatorSet = AnimatorSet().apply {
                play(shrinkAnimation)
                duration = 1000L
                interpolator = AccelerateDecelerateInterpolator()
                doOnEnd {
                    binding.imageView2.scaleType = ImageView.ScaleType.FIT_XY
                    animating = false
                }
            }

            binding.cardView.isClickable = true
            binding.cardView.isFocusable = true
            binding.cardView.setOnClickListener {
                expansionAnimatorSet.start()
                if (!animating) {
                    animating = true
                    when (expanded) {
                        false -> expansionAnimatorSet.start()
                        else -> shrinkAnimatorSet.start()
                    }
                    expanded = !expanded
                }
            }
        }

        return binding.root
    }
}