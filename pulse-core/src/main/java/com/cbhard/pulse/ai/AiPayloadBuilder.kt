package com.cbhard.pulse.ai

import android.content.Context
import android.os.Build
import android.util.Log
import com.cbhard.pulse.model.PulseEvent
import org.json.JSONArray
import org.json.JSONObject

internal class AiPayloadBuilder(context: Context) {

    // 1. The engine is initialized here, using the Context to load the TFLite assets
    private val inferenceEngine = PulseInferenceEngine(context)

    fun generateReport(anomaly: PulseEvent.Anomaly, timeline: List<PulseEvent>) {
        Thread {
            try {
                val root = JSONObject()

                val contextObj = JSONObject().apply {
                    put("os_version", Build.VERSION.SDK_INT)
                    put("device_model", Build.MODEL)
                    put("manufacturer", Build.MANUFACTURER)
                }
                root.put("environment", contextObj)

                val anomalyObj = JSONObject().apply {
                    put("type", anomaly.type)
                    put("description", anomaly.description)
                    put("timestamp", anomaly.timestamp)
                }
                root.put("trigger", anomalyObj)

                val timelineArray = JSONArray()
                timeline.forEach { event ->
                    timelineArray.put(JSONObject().apply { put("log", event.toString()) })
                }
                root.put("context_window", timelineArray)

                // 2. The builder triggers the engine's analyze method!
                Log.d("[PulseCore]", "Feeding telemetry timeline to AI Inference Engine...")
                val recommendation = inferenceEngine.analyze(root)

                Log.e("[PulseCore] Diagnostics", "\n$recommendation")

            } catch (e: Exception) {
                Log.e("[PulseCore]", "Failed to build AI payload", e)
            }
        }.start()
    }
}