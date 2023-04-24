package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.R
import com.app.receiptscanner.adapters.LibraryAdapter
import com.app.receiptscanner.databinding.FragmentReceiptLibraryBinding
import com.app.receiptscanner.viewmodels.*

class ReceiptLibraryFragment : Fragment() {
    private var _binding: FragmentReceiptLibraryBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private var tracker: SelectionTracker<Long>? = null
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
    private var menu: Menu? = null
    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.library_menu, menu)
            menu.setGroupVisible(R.id.selectedMenu, false)
            this@ReceiptLibraryFragment.menu = menu
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.calculateStatistics -> {
                    findNavController()
                        .navigate(R.id.action_userMainFragment_to_statisticsPromptFragment)
                    true
                }
                R.id.receiptStatistics -> {
                    val ids = tracker?.let {
                        it.selection.map { value -> value.toInt() }
                    }
                    ids?.let {
                        receiptViewmodel.loadReceiptsById(it) { receipts ->
                            statisticsViewmodel.setReceipts(receipts)
                            findNavController()
                                .navigate(R.id.action_userMainFragment_to_statisticsResultFragment)
                        }
                    }
                    true
                }
                R.id.receiptDeletion -> {
                    true
                }
                R.id.addToGroup -> {
                    true
                }
                else -> false
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptLibraryBinding.inflate(inflater, container, false)

        // Once the screen has been created, the user's receipts are loaded
        // from the database into memory
        receiptViewmodel.loadUserReceipts()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initially the adapter's data is empty. This is fine as it will be
        // populated once the receipts have been loaded
        val adapter = LibraryAdapter(activity, listOf()) {
            receiptViewmodel.setNormalizedReceipt(it)
            findNavController().navigate(R.id.action_userMainFragment_to_receiptFragment)
        }
        adapter.setHasStableIds(true)

        // Once the receipts have been loaded, they are shown to the user in the form of a list
        binding.libraryRecyclerView.adapter = adapter
        binding.libraryRecyclerView.layoutManager = LinearLayoutManager(activity)

        // The previously defined menu is added once the screen has been fully created
        activity.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Detects any changes in the user's receipts, reloading the list
        // and tracker upon any change
        receiptViewmodel.userReceipts.observe(viewLifecycleOwner) {

            // As the data is loaded from the database, this must be done on a different thread,
            // meaning that a function must be passed to be executed after the receipts are loaded
            receiptViewmodel.loadReceiptData {
                adapter.setReceipts(it)
                tracker = adapter.getTracker(binding.libraryRecyclerView)

                tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                    // Changes the menu shown based on whether any receipts are selected
                    // Selection -> Shows options relating to manipulating said selection
                    // No Selection -> Only shows options to calculate statistics
                    override fun onSelectionChanged() {
                        when (tracker?.selection?.isEmpty) {
                            false -> {
                                menu?.setGroupVisible(R.id.selectedMenu, true)
                                menu?.setGroupVisible(R.id.defaultMenu, false)
                            }
                            else -> {
                                menu?.setGroupVisible(R.id.selectedMenu, false)
                                menu?.setGroupVisible(R.id.defaultMenu, true)
                            }
                        }
                    }
                })
            }
        }

    }
}