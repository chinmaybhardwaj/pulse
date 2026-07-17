package com.cbhard.pulse.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

internal class PulseInferenceEngine(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var isInitialized = false
    private val vocabulary = mutableMapOf<String, Int>()

    private val maxSequenceLength = 256

    init {
        loadVocabulary()
        initializeTfLite()
    }

    private fun loadVocabulary() {
        try {
            val inputStream = context.assets.open("pulse_vocab.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val jsonStr = String(buffer, Charset.forName("UTF-8"))
            val jsonObject = JSONObject(jsonStr)

            jsonObject.keys().forEach { key ->
                vocabulary[key] = jsonObject.getInt(key)
            }
            Log.d("[PulseCore] AI", "Loaded vocabulary with ${vocabulary.size} tokens.")
        } catch (e: Exception) {
            Log.e("[PulseCore] AI", "Failed to load vocabulary JSON.", e)
        }
    }

    private fun initializeTfLite() {
        try {
            val modelBuffer = loadModelFile("pulse_telemetry_classifier.tflite")
            // Initialize using the standalone Interpreter and its Options
            val options = Interpreter.Options()
            interpreter = Interpreter(modelBuffer, options)
            isInitialized = true
            Log.d("[PulseCore] AI", "Standalone TFLite Engine successfully loaded.")
        } catch (e: Exception) {
            Log.e("[PulseCore] AI", "Failed to load TFLite model.", e)
        }
    }

    private fun loadModelFile(modelName: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.declaredLength
        )
    }

    fun analyze(payload: JSONObject): com.cbhard.pulse.model.FixRecommendation {
        if (!isInitialized || interpreter == null) {
            return fallbackRecommendation()
        }

        val triggerObj = payload.optJSONObject("trigger")
        val type = triggerObj?.optString("type", "") ?: ""
        val desc = triggerObj?.optString("description", "") ?: ""

        val textToAnalyze = "Anomaly $type $desc"
        val tokenizedSequence = tokenizePayload(textToAnalyze)

        // --- ML DEBUG LOGS ---
        Log.d("[PulseCore] AI", "Raw Text: $textToAnalyze")
        Log.d("[PulseCore] AI", "Tokens: ${tokenizedSequence.take(10).joinToString(", ")}...")
        // ---------------------

        // --- THE FIX: Wrap in arrayOf() to create the [1, 256] batch dimension ---
        val inputTensor = arrayOf(tokenizedSequence)
        // -------------------------------------------------------------------------

        val outputTensor = Array(1) { FloatArray(3) }

        try {
            // Now the shapes perfectly match: [1, 256] -> [1, 3]
            interpreter?.run(inputTensor, outputTensor)
        } catch (e: Exception) {
            Log.e("[PulseCore] AI", "Inference execution failed.", e)
            return fallbackRecommendation()
        }

        return decodeOutputTensor(outputTensor[0])
    }

    private fun tokenizePayload(payloadText: String): IntArray {
        val tokenizedArray = IntArray(maxSequenceLength) { 0 }

        val cleanText = payloadText.lowercase().replace(Regex("[^a-z0-9\\s]"), " ")
        val words = cleanText.split(Regex("\\s+")).filter { it.isNotEmpty() }

        for (i in words.indices) {
            if (i >= maxSequenceLength) break
            tokenizedArray[i] = vocabulary[words[i]] ?: 1
        }

        return tokenizedArray
    }

    private fun decodeOutputTensor(probabilities: FloatArray): com.cbhard.pulse.model.FixRecommendation {
        var maxIndex = 0
        var maxProb = probabilities[0]

        for (i in 1 until probabilities.size) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        return when (maxIndex) {
            0 -> com.cbhard.pulse.model.FixRecommendation(
                anomalyType = "ExcessiveRecomposition",
                rootCause = "A state variable is being read and modified within the same Compose phase.",
                suggestedFix = "Move state mutations inside a SideEffect block (e.g., LaunchedEffect) or a user callback (e.g., onClick).",
                confidenceScore = maxProb
            )
            1 -> com.cbhard.pulse.model.FixRecommendation(
                anomalyType = "UIFreeze",
                rootCause = "A synchronous operation blocked the Choreographer thread.",
                suggestedFix = "Wrap the blocking operation in a coroutine using `withContext(Dispatchers.IO)`.",
                confidenceScore = maxProb
            )
            2 -> com.cbhard.pulse.model.FixRecommendation(
                anomalyType = "MemoryLeak",
                rootCause = "An Activity was destroyed but its context is still referenced in memory.",
                suggestedFix = "Clear static references in `onDestroy()` or wrap long-lived references in a `WeakReference`.",
                confidenceScore = maxProb
            )
            else -> fallbackRecommendation()
        }
    }

    private fun fallbackRecommendation() = com.cbhard.pulse.model.FixRecommendation(
        anomalyType = "Pending",
        rootCause = "Model is warming up...",
        suggestedFix = "TFLite runtime is currently initializing.",
        confidenceScore = 0.0f
    )
}