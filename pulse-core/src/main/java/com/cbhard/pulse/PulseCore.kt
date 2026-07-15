package com.cbhard.pulse

import android.app.Application
import android.util.Log

class PulseCore private constructor(private val application: Application) {

    // 1. Expose the buffer internally
    internal val buffer = PulseBuffer(capacity = 150)
    internal val leakAnalyzer = LeakAnalyzer(buffer)
    internal val jankMonitor = JankMonitor(buffer)

    init {
        Log.d("[PulseCore]", "Pulse SDK initialized in background.")

        // Boot up our monitoring systems
        val lifecycleMonitor = LifecycleMonitor(buffer, leakAnalyzer, jankMonitor)
        application.registerActivityLifecycleCallbacks(lifecycleMonitor)
    }

    companion object {
        @Volatile
        private var instance: PulseCore? = null

        /**
         * Internal initialization called by App Startup.
         */
        internal fun initialize(application: Application): PulseCore {
            return instance ?: synchronized(this) {
                instance ?: PulseCore(application).also { instance = it }
            }
        }

        /**
         * Public accessor if developers ever need manual API calls in the future.
         */
        fun getInstance(): PulseCore {
            return instance ?: throw IllegalStateException("PulseCore is not initialized")
        }
    }
}