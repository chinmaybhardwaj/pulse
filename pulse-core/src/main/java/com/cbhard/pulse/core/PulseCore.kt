package com.cbhard.pulse.core

import android.app.Application
import android.util.Log
import com.cbhard.pulse.ai.AiPayloadBuilder
import com.cbhard.pulse.buffer.PulseBuffer
import com.cbhard.pulse.monitor.ComposeMonitor
import com.cbhard.pulse.monitor.JankMonitor
import com.cbhard.pulse.monitor.LeakAnalyzer
import com.cbhard.pulse.monitor.LifecycleMonitor

class PulseCore private constructor(private val application: Application) {

    // 1. Initialize the AI Builder with the host app's Context
    internal val aiPayloadBuilder = AiPayloadBuilder(application)
    // 2. Expose the buffer internally
    internal val buffer = PulseBuffer(capacity = 150)
    internal val leakAnalyzer = LeakAnalyzer(buffer, aiPayloadBuilder)
    internal val jankMonitor = JankMonitor(buffer, aiPayloadBuilder)
    internal val composeMonitor = ComposeMonitor(buffer, aiPayloadBuilder)

    private var isShutdown = false
    private val lifecycleMonitor = LifecycleMonitor(buffer, leakAnalyzer, jankMonitor)

    init {
        PulseSafeguard.execute("[PulseCore] Init") {
            Log.d("[PulseCore]", "Pulse SDK initialized. Telemetry systems online.")
            application.registerActivityLifecycleCallbacks(lifecycleMonitor)
        }
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

    /**
     * Dismantles all system hooks and clears memory.
     */
    fun shutdown() {
        if (isShutdown) return
        isShutdown = true

        PulseSafeguard.execute("[PulseCore] Shutdown") {
            // 1. Unregister lifecycle callbacks so we stop tracking Activities
            application.unregisterActivityLifecycleCallbacks(lifecycleMonitor)
            // 2. Clear the time-traveling buffer to free up heap memory
            buffer.clear()
            Log.d("[PulseCore]", "Pulse SDK successfully dismantled. Zero overhead restored.")
        }
    }
}