package com.android.samples.biometricsample

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment

fun Fragment.showToast(message: String) {
    context?.showToast(message = message)
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showDialog(dialogFragment: DialogFragment) {
    if (isAdded) {
        dialogFragment.show(childFragmentManager, dialogFragment.javaClass.simpleName)
    }
}