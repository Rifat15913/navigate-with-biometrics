package com.android.samples.biometricsample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.samples.biometricsample.databinding.FragmentFirstBinding
import timber.log.Timber
import java.util.concurrent.Executor

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var binding: FragmentFirstBinding? = null

    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var biometricPromptInfo: BiometricPrompt.PromptInfo? = null
    private var authLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentFirstBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureBiometricPrompt()

        binding?.buttonFirst?.setOnClickListener {
            biometricPromptInfo?.let {
                biometricPrompt?.authenticate(it)
            }
        }
    }

    private fun configureBiometricPrompt() {
        context?.apply {
            executor = ContextCompat.getMainExecutor(this)

            // just to show the device status regarding biometric features availability
            // showDeviceBiometricFeaturesStatus()
        }

        executor?.let {
            biometricPrompt = BiometricPrompt(
                this,
                it,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Timber.d("Biometric Auth :: Authentication error: $errString")

                        showToast("Authentication error: $errString")
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Timber.d("Biometric Auth :: Authentication succeeded!")

                        showToast("Authentication succeeded!")
                        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Timber.d("Biometric Auth :: Authentication failed")

                        showToast("Authentication failed")
                    }
                },
            )
        }

        /*
        * Allows user to authenticate using either a Class 3 biometric or
        * their lock screen credential (PIN, pattern, or password).
        *
        * N.B: Can't call setNegativeButtonText() and setAllowedAuthenticators(... or
        * DEVICE_CREDENTIAL) at the same time.
        * */
        biometricPromptInfo = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P
            || Build.VERSION.SDK_INT > Build.VERSION_CODES.Q
        ) {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_info_title))
                .setSubtitle(getString(R.string.biometric_prompt_info_subtitle))
                .setAllowedAuthenticators(
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build()
        } else {
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_info_title))
                .setSubtitle(getString(R.string.biometric_prompt_info_subtitle))
                .setAllowedAuthenticators(
                    BIOMETRIC_WEAK or DEVICE_CREDENTIAL
                )
                .setConfirmationRequired(false)
                .build()
        }
    }

    @Suppress("DEPRECATION")
    private fun Context.showDeviceBiometricFeaturesStatus() {
        BiometricManager.from(this).let {
            when (it.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    showMessageAndProceed(resId = R.string.biometric_status_is_fine)
                }

                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    showMessageAndProceed(resId = R.string.no_biometric_features_available)
                }

                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    showMessageAndProceed(resId = R.string.biometric_features_are_currently_unavailable)
                }

                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    showMessageAndProceed(
                        resId = R.string.biometric_status_is_none_enrolled,
                        callback = {
                            authLauncher = registerForActivityResult(
                                ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                when (result.resultCode) {
                                    Activity.RESULT_OK -> {
                                        // get result here in result.data
                                        showMessageAndProceed(
                                            resId = R.string.new_biometric_has_been_enrolled,
                                        )
                                        biometricPromptInfo?.let { info ->
                                            biometricPrompt?.authenticate(info)
                                        }
                                    }

                                    Activity.RESULT_CANCELED -> {
                                        showMessageAndProceed(
                                            resId = R.string.new_biometric_enrollment_has_been_cancelled,
                                        )
                                    }

                                    else -> {
                                        showMessageAndProceed(
                                            resId = R.string.wtf_case,
                                        )
                                    }
                                }
                            }

                            // Prompts the user to create credentials that your app accepts.
                            authLauncher?.launch(
                                Intent().apply {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        action = Settings.ACTION_BIOMETRIC_ENROLL
                                        putExtra(
                                            EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
                                        )
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                                        && Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                                    ) {
                                        action = Settings.ACTION_FINGERPRINT_ENROLL
                                    } else {
                                        action = Settings.ACTION_SECURITY_SETTINGS
                                    }
                                },
                            )
                        },
                    )
                }

                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                    showMessageAndProceed(resId = R.string.security_update_is_required)
                }

                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                    showMessageAndProceed(resId = R.string.biometric_status_is_unsupported)
                }

                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                    showMessageAndProceed(resId = R.string.biometric_status_is_unknown)
                }
            }
        }
    }

    private fun Context.showMessageAndProceed(
        @StringRes resId: Int,
        callback: (() -> Unit)? = null,
    ) {
        val message = getString(resId)
        Timber.e("Biometric Prompt :: $message")

        showToast(message = message)
        callback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}