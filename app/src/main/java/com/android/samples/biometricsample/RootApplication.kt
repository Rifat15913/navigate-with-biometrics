package com.android.samples.biometricsample

import androidx.multidex.MultiDexApplication
import timber.log.Timber

class RootApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun createStackElementTag(element: StackTraceElement): String {
                        return "${super.createStackElementTag(element)} | Method:${element
                            .methodName} | Line:${element.lineNumber}"
                    }
                }
            )
        }
    }
}