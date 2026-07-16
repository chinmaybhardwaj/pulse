package com.cbhard.pulse

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

internal class ComposeMonitor(private val buffer: PulseBuffer) {

    // Tracks: ComponentName -> Pair<LastResetTimestamp, RecompositionCount>
    private val recomposeCounts = ConcurrentHashMap<String, Pair<Long, Int>>()
    
    // Anything recomposing more than 20 times in a single second is usually a state loop bug
    private val ANOMALY_THRESHOLD = 20 

    fun recordRecomposition(componentName: String) {
        val now = System.currentTimeMillis()
        val currentData = recomposeCounts[componentName]

        if (currentData == null) {
            recomposeCounts[componentName] = Pair(now, 1)
            return
        }

        val (lastTime, count) = currentData
        val timeDelta = now - lastTime

        if (timeDelta < 1000) { // Still within the same 1-second window
            val newCount = count + 1
            recomposeCounts[componentName] = Pair(lastTime, newCount)

            // Did we hit the danger zone?
            if (newCount == ANOMALY_THRESHOLD) {
                Log.w("PulseCore", "🔥 COMPOSE MELTDOWN: $componentName recomposed $newCount times in < 1s!")
                
                val anomaly = PulseEvent.Anomaly("ExcessiveRecomposition", "$componentName spinning at >$ANOMALY_THRESHOLD RPS")
                buffer.record(anomaly)
                
                // Trigger our AI payload builder
                AiPayloadBuilder.generateReport(anomaly, buffer.extractTimeline())
            }
        } else {
            // A second has passed, reset the bucket
            recomposeCounts[componentName] = Pair(now, 1)
        }
    }
}