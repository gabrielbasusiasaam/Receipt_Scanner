package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentUserMainBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class UserMainFragment : Fragment() {
    private var _binding: FragmentUserMainBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val args: UserMainFragmentArgs by navArgs()
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
        _binding = FragmentUserMainBinding.inflate(inflater, container, false)
        receiptViewmodel.setUserId(args.userId)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val names = arrayListOf("Library", "Groups", "Settings")
        val icons = arrayListOf(
            R.drawable.ic_baseline_library_books_24,
            R.drawable.ic_baseline_collections_bookmark_24,
            R.drawable.ic_baseline_settings_24
        )
        val adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        var page = 0
        activity.setSupportActionBar(binding.userMainToolbar)
        binding.userViewPager.adapter = adapter
        binding.userViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.userMainToolbar.title = names[position]
                page = position
                if (position < 2) binding.floatingActionButton.show()
                else binding.floatingActionButton.hide()
            }
        })
        TabLayoutMediator(binding.userTabs, binding.userViewPager) { tab, position ->
            tab.text = names[position]
            tab.setIcon(icons[position])
        }.attach()
        binding.userTabs.tabGravity = TabLayout.GRAVITY_FILL

        binding.floatingActionButton.setOnClickListener {
            when (page) {
                0 -> findNavController().navigate(R.id.action_userMainFragment_to_receiptCreationFragment)
                1 -> findNavController().navigate(R.id.action_userMainFragment_to_receiptGroupCreationFragment)
            }

        }
    }
}

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ReceiptLibraryFragment()
            1 -> ReceiptGroupFragment()
            else -> SettingsFragment()
        }
    }

}