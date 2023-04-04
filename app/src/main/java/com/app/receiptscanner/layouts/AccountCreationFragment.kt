package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.database.SecurityUtil.ILLEGAL_CHARACTER
import com.app.receiptscanner.database.SecurityUtil.PASSWORDS_DO_NOT_MATCH
import com.app.receiptscanner.database.SecurityUtil.PASSWORD_TOO_SHORT
import com.app.receiptscanner.database.SecurityUtil.USERNAME_EMPTY
import com.app.receiptscanner.database.SecurityUtil.USERNAME_TAKEN
import com.app.receiptscanner.database.SecurityUtil.checkFlag
import com.app.receiptscanner.databinding.FragmentAccountCreationBinding
import com.app.receiptscanner.viewmodels.AccountCreationViewmodel
import com.app.receiptscanner.viewmodels.AccountCreationViewmodelFactory
import com.app.receiptscanner.viewmodels.ReceiptApplication
import kotlinx.coroutines.launch

class AccountCreationFragment : Fragment() {
    private var _binding: FragmentAccountCreationBinding? = null
    private val binding get() = _binding!!
    private val application by lazy { requireActivity().application as ReceiptApplication }
    private val viewmodel: AccountCreationViewmodel by viewModels {
        AccountCreationViewmodelFactory(application.userRepository, application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountCreationBinding.inflate(inflater, container, false)
        binding.accountCreationToolbar.setTitle(R.string.account_creation)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // After the user types, the error message shown to them will be reset
        binding.usernameField.editText?.doAfterTextChanged {
            binding.usernameField.error = null
        }
        binding.passwordField.editText?.doAfterTextChanged {
            binding.passwordField.error = null
        }
        binding.passwordRetryField.editText?.doAfterTextChanged {
            binding.passwordRetryField.error = null
        }

        binding.createAccountButton.setOnClickListener {
            val username = binding.usernameField.editText?.text.toString()
            val password = binding.passwordField.editText?.text.toString()
            val passwordRetry = binding.passwordRetryField.editText?.text.toString()
            lifecycleScope.launch {
                // Tests if the user's entered credentials are valid to be inserted
                val result = viewmodel.validate(username, password, passwordRetry)
                when (result.isSuccess) {
                    /**
                     * If the credentials are valid, the user's details are inserted into the
                     * User table, and the user is automatically logged in.
                     */
                    true -> {
                        val allowBiometrics = binding.biometricSwitch.isChecked
                        val user = viewmodel.createUser(username, password, allowBiometrics)
                        val action = AccountCreationFragmentDirections
                            .actionAccountCreationFragmentToUserMainFragment2(user.id)
                        findNavController().navigate(action)
                    }
                    /**
                     * If the credentials are invalid, the appropriate error messages are shown to
                     * the user.
                     */
                    false -> {
                        if (checkFlag(result.reason, USERNAME_TAKEN)) {
                            binding.usernameField.error = "Username is taken"
                        }
                        if (checkFlag(result.reason, ILLEGAL_CHARACTER)) {
                            binding.usernameField.error =
                                "Username must contain only alphanumeric characters"
                        }
                        if (checkFlag(result.reason, USERNAME_EMPTY)) {
                            binding.usernameField.error = "Enter a username"
                        }
                        if (checkFlag(result.reason, PASSWORD_TOO_SHORT)) {
                            binding.passwordField.error = "Password must be at least 8 characters"
                        }
                        if (checkFlag(result.reason, PASSWORDS_DO_NOT_MATCH)) {
                            binding.passwordRetryField.error = "Passwords do not match"
                        }
                    }
                }
            }
        }
    }
}