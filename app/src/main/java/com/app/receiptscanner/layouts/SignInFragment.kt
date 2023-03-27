package com.app.receiptscanner.layouts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.receiptscanner.R
import com.app.receiptscanner.database.SecurityUtil.PASSWORD_INCORRECT
import com.app.receiptscanner.database.SecurityUtil.USERNAME_DOES_NOT_EXIST
import com.app.receiptscanner.database.User
import com.app.receiptscanner.databinding.FragmentSignInBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.SignInViewmodel
import com.app.receiptscanner.viewmodels.SignInViewmodelFactory
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val viewmodel: SignInViewmodel by viewModels {
        SignInViewmodelFactory(application.userRepository, application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.signInToolbar.setTitle(R.string.sign_in)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var biometricSupported = true
        val executor = ContextCompat.getMainExecutor(activity)
        var user: User? = null
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val action = SignInFragmentDirections
                    .actionSignInFragmentToUserMainFragment2(user!!.id)
                findNavController().navigate(action)
            }
        }
        val prompt = BiometricPrompt(this, executor, callback)
        val biometricManager = BiometricManager.from(activity)
        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                it.resultCode
            }
        when (biometricManager.canAuthenticate(DEVICE_CREDENTIAL or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                    )
                }
                try {
                    activityResultLauncher.launch(enrollIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        activity,
                        "Biometrics are not supported for this device",
                        Toast.LENGTH_LONG
                    ).show()
                    biometricSupported = false
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> biometricSupported = false
            else -> {}
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Account login")
            .setDescription("Log in using your device credentials")
            .setNegativeButtonText("Cancel")
            .build()

        binding.signInButton.setOnClickListener {
            val username = binding.usernameField.editText?.text.toString().trim()
            val password = binding.passwordField.editText?.text.toString()
            lifecycleScope.launch {
                val result = viewmodel.verify(username, password)
                when (result.isSuccess) {
                    true -> {
                        val action = SignInFragmentDirections
                            .actionSignInFragmentToUserMainFragment2(result.data!!.id)
                        findNavController().navigate(action)
                    }
                    false -> {
                        if (result.reason and USERNAME_DOES_NOT_EXIST == USERNAME_DOES_NOT_EXIST) {
                            binding.usernameField.error = "Username does not exist"
                        }
                        if (result.reason and PASSWORD_INCORRECT == PASSWORD_INCORRECT) {
                            binding.passwordField.error = "Password is incorrect"
                        }
                    }
                }
            }
        }

        binding.biometricButton.setOnClickListener {
            lifecycleScope.launch {
                val username = binding.usernameField.editText?.text.toString().trim()
                when (viewmodel.checkUsernameExists(username)) {
                    false -> {
                        binding.usernameField.error = "Username does not exist"
                    }
                    else -> {
                        user = viewmodel.getUser(username)
                        if (!user!!.allowsBiometrics) {
                            Toast.makeText(
                                activity,
                                "Account does not support biometrics",
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (!biometricSupported) {
                            Toast.makeText(
                                activity,
                                "Device does not support biometrics",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            prompt.authenticate(promptInfo)
                        }
                    }
                }
            }
        }
    }
}