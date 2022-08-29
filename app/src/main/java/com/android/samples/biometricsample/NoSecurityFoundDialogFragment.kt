package com.android.samples.biometricsample

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class NoSecurityFoundDialogFragment(
    private val title: String?,
    private val securitySettingsLauncher: ActivityResultLauncher<Intent>?,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            AlertDialog.Builder(it)
                .setMessage(title)
                .setPositiveButton(
                    R.string.go_to_settings,
                ) { dialog, _ ->
                    securitySettingsLauncher?.launch(
                        Intent().apply {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                action = Settings.ACTION_BIOMETRIC_ENROLL
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
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

                    dialog.dismiss()
                }
                .setNegativeButton(
                    R.string.back,
                ) { dialog, _ ->
                    dialog.dismiss()
                }.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            val fragmentTransaction = manager.beginTransaction()
            fragmentTransaction.add(this, tag)
            fragmentTransaction.commitAllowingStateLoss()
        }
    }
}