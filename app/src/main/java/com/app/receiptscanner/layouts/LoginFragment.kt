package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.FragmentLoginBinding
import com.app.receiptscanner.databinding.FragmentLoginPage1Binding
import com.app.receiptscanner.databinding.FragmentLoginPage2Binding
import com.app.receiptscanner.databinding.FragmentLoginPage3Binding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val mainActivity by lazy { requireActivity() as MainActivity }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.setSupportActionBar(binding.loginToolbar)
        mainActivity.title = "Login"

        binding.loginSlides.adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        TabLayoutMediator(binding.tabLayout, binding.loginSlides) { tab, position ->
            tab.text = position.toString()
        }.attach()
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        binding.createAccountButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_accountCreationFragment)
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }

    }

    inner class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount() = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> PageFragment1()
                1 -> PageFragment2()
                else -> PageFragment3()
            }
        }

    }

    class PageFragment1 : Fragment() {
        private var _binding: FragmentLoginPage1Binding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentLoginPage1Binding.inflate(layoutInflater, container, false)
            return binding.root
        }
    }

    class PageFragment2 : Fragment() {
        private var _binding: FragmentLoginPage2Binding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentLoginPage2Binding.inflate(layoutInflater, container, false)
            return binding.root
        }
    }

    class PageFragment3 : Fragment() {
        private var _binding: FragmentLoginPage3Binding? = null
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentLoginPage3Binding.inflate(layoutInflater, container, false)
            return binding.root
        }
    }
}