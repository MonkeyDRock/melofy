package com.example.melofy

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MelofyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Set the debug App Check token so Firebase can verify our test phone requests
            System.setProperty(
                "net.security.appcheck.debug.token",
                "AdpetEbbIDg0zB7MBjWDTJ8da1Qwn_LTmj8qfMnsFe7IOZXFOMoAb_XryCtDmsZRtDAgUR_m4r3KrRql7M79oe9fufjracTUBA2IIVrR5iz5dYbSLeRlxr7nkrA7L-xmEnFW0EVqeRiBehsftNtZS-nYc"
            )
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
