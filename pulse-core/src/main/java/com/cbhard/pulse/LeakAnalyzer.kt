package com.cbhard.pulse

import android.app.Activity
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import java.lang.ref.WeakReference

internal class LeakAnalyzer(private val buffer: PulseBuffer) {

    // A dedicated background thread so we NEVER block the host app's UI
    private val handlerThread = HandlerThread("Pulse-Leak-Analyzer").apply { start() }
    private val handler = Handler(handlerThread.looper)

    fun watch(activity: Activity) {
        val activityName = activity.javaClass.simpleName

        // Wrap the activity in a WeakReference. 
        // A WeakReference will NOT prevent the Garbage Collector from destroying the object.
        val weakRef = WeakReference(activity)

        handler.postDelayed({
            // 1. Ask the JVM to run garbage collection (It's a hint, but usually respected)
            Runtime.getRuntime().gc()

            // 2. Give the GC a fraction of a second to finalize objects
            Thread.sleep(100)

            // 3. Check if the Activity survived the purge
            if (weakRef.get() != null) {
                Log.e(
                    "[PulseCore]",
                    "🚨 MEMORY LEAK DETECTED: $activityName was not garbage collected!"
                )

                // Record this in our AI Black Box
                val anomaly = PulseEvent.Anomaly("MemoryLeak", "$activityName retained in memory")
                buffer.record(anomaly)
                // Trigger our AI payload builder
                AiPayloadBuilder.generateReport(anomaly, buffer.extractTimeline())
                // TODO (V2): This is where we would trigger the lightweight heap path extraction
            } else {
                Log.v("[PulseCore]", "✅ $activityName cleanly collected.")
            }
        }, 5000) // Wait 5 seconds after the host app destroys the activity
    }

    /**
     * Shuts down the background thread used for leak analysis.
     */
    fun shutdown() {
        handler.removeCallbacksAndMessages(null)
        handlerThread.quitSafely()
    }
}