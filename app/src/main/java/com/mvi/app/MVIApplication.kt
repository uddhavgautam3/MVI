package com.mvi.app

import android.app.Application
import android.os.StrictMode
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber


@HiltAndroidApp
class MVIApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        enableStrictMode()

        //Android Release : "d0e305f6-61df-4b19-9230-522829dbc071"
        //Android EQA: "2f81db8d-ca04-44fd-907c-5280b958bb9e"
        //Android Feature Feature1 Debug: "0bbfe0d8-bc54-4930-aedc-9d36dbc889a3"
        val appCenterSecret = if(BuildConfig.APPLICATION_ID.contains(".enterpriseQa")) {
            "2f81db8d-ca04-44fd-907c-5280b958bb9e"
        } else if(BuildConfig.APPLICATION_ID.contains(".debug")) {
            "0bbfe0d8-bc54-4930-aedc-9d36dbc889a3"
        } else {
            //release
            "d0e305f6-61df-4b19-9230-522829dbc071"
        }
        AppCenter.start(
            this, appCenterSecret, //MVI Dev Refactor
            Analytics::class.java, Crashes::class.java
        )

    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
