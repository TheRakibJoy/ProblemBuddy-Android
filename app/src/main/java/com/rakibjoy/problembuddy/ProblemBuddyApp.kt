package com.rakibjoy.problembuddy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ProblemBuddyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Analytics always on — debug and release both emit events, which lets
        // us validate funnels locally before shipping.
        Firebase.analytics.setAnalyticsCollectionEnabled(true)
        // Crashlytics off in debug to keep stack traces from local repro runs
        // out of the production dashboard; on in release so side-loaded APK
        // crashes still reach us.
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }
}
