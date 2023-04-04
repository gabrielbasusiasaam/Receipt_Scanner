package com.app.receiptscanner.layouts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.adapters.LibraryAdapter
import com.app.receiptscanner.databinding.FragmentReceiptLibraryBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory

class ReceiptLibraryFragment : Fragment() {
    private var _binding: FragmentReceiptLibraryBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val viewmodel: ReceiptViewmodel by activityViewModels {
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
        _binding = FragmentReceiptLibraryBinding.inflate(inflater, container, false)
        // memory
        // Once the screen has been created, the user's receipts are loaded from the database into
        viewmodel.loadUserReceipts()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = LibraryAdapter(activity, listOf())
        binding.libraryRecyclerView.adapter = adapter
        binding.libraryRecyclerView.layoutManager = LinearLayoutManager(activity)
        // Once the receipts have been loaded, they are shown to the user in the form of a list
        viewmodel.userReceipts.observe(viewLifecycleOwner) {
            Log.e("Observer", "CALLED!")
            viewmodel.loadReceiptData {
                adapter.setReceipts(it)
            }
        }
    }
}