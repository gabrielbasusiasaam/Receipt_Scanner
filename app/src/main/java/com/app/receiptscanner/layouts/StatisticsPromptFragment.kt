package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentStatisticsPromptBinding
import com.app.receiptscanner.viewmodels.*
import java.text.SimpleDateFormat
import java.util.*

class StatisticsPromptFragment : Fragment() {
    private var _binding: FragmentStatisticsPromptBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val startDate = arrayOf<Int?>(null, null, null)
    private val endDate = arrayOf<Int?>(null, null, null)
    private val receiptViewmodel: ReceiptViewmodel by activityViewModels {
        ReceiptViewmodelFactory(
            application.userRepository,
            application.receiptRepository,
            application
        )
    }
    private val statisticsViewmodel: StatisticsViewmodel by activityViewModels {
        StatisticsViewmodelFactory(
            application.receiptRepository,
            application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsPromptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val providers = arrayOf("All", "Marks and Spencers", "Lidl", "Morrisons", "Sainsbury's")
        val arrayAdapter = ArrayAdapter(activity, R.layout.list_item, providers)
        (binding.providerSelection.editText as? AutoCompleteTextView)?.let {
            it.setAdapter(arrayAdapter)
            it.setText(providers[0], false)
        }

        binding.startDate.setOnClickListener {
            val dialog = DateSelectionDialog.newInstance(
                startDate[0],
                startDate[1],
                startDate[2]
            ) { _, p1, p2, p3 ->
                startDate.updateTime(p1, p2, p3)
                val date = getTimeFromDate(startDate, arrayOf(0, 0, 0))
                val formatted = date.format("dd/MM/yyyy", Locale.UK)
                val outputText = getString(R.string.date_output, "Start", formatted)
                binding.startDate.text = outputText
            }
            dialog.show(childFragmentManager, "date_picker")
        }

        binding.endDate.setOnClickListener {
            val dialog =
                DateSelectionDialog.newInstance(
                    endDate[0],
                    endDate[1],
                    endDate[2]
                ) { _, p1, p2, p3 ->
                    endDate.updateTime(p1, p2, p3)
                    val date = getTimeFromDate(endDate, arrayOf(0, 0, 0))
                    val formatted = date.format("dd/MM/yyyy", Locale.UK)
                    val outputText = getString(R.string.date_output, "End", formatted)
                    binding.endDate.text = outputText
                }
            dialog.show(childFragmentManager, "date_picker")
        }

        binding.calculateButton.setOnClickListener {
            if (endDate.contains(null) || startDate.contains(null)) {
                Toast.makeText(activity, "Invalid dates", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val start = getTimeFromDate(startDate, arrayOf(0, 0, 0))
            val end = getTimeFromDate(endDate, arrayOf(23, 59, 59))
            if (end < start) {
                Toast.makeText(activity, "Invalid period", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            receiptViewmodel.getReceiptsByDate(start, end) {
                if (it.isEmpty()) {
                    Toast.makeText(
                        activity,
                        "There are no receipts made in this period",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@getReceiptsByDate
                }
                statisticsViewmodel.setReceipts(it)
                findNavController().navigate(R.id.action_statisticsPromptFragment_to_statisticsResultFragment)
            }
        }
    }

    private fun Array<Int?>.updateTime(year: Int, month: Int, day: Int) {
        assert(this.size == 3)
        this[0] = year
        this[1] = month
        this[2] = day
    }

    private fun getTimeFromDate(date: Array<Int?>, time: Array<Int>): Date {
        val calendar = Calendar.getInstance()
        calendar.set(date[0]!!, date[1]!!, date[2]!!, time[0], time[1], time[2])
        return calendar.time
    }

    private fun Date.format(pattern: String, locale: Locale): String {
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(this)
    }
}