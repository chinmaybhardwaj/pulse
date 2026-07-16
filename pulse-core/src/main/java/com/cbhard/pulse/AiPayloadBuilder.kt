package com.cbhard.pulse

import android.os.Build
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

internal object AiPayloadBuilder {

    // Instantiate our Inference Engine
    private val inferenceEngine = PulseInferenceEngine()

    fun generateReport(anomaly: PulseEvent.Anomaly, timeline: List<PulseEvent>) {
        Thread {
            try {
                val root = JSONObject()

                // 1. Build Context
                val contextObj = JSONObject().apply {
                    put("os_version", Build.VERSION.SDK_INT)
                    put("device_model", Build.MODEL)
                }
                root.put("environment", contextObj)

                // 2. Build Trigger
                val anomalyObj = JSONObject().apply {
                    put("type", anomaly.type)
                    put("description", anomaly.description)
                }
                root.put("trigger", anomalyObj)

                // 3. Build Timeline
                val timelineArray = JSONArray()
                timeline.forEach { timelineArray.put(JSONObject().apply { put("log", it.toString()) }) }
                root.put("context_window", timelineArray)

                // --- NEW: Run Inference ---
                Log.d("[PulseCore]", "Feeding telemetry to AI Inference Engine...")
                val recommendation = inferenceEngine.analyze(root)
                
                // Output the final, actionable result to the developer
                Log.e("[PulseCore] Diagnostics", "\n$recommendation")

            } catch (e: Exception) {
                Log.e("[PulseCore]", "Failed to build AI payload", e)
            }
        }.start()
    }
}