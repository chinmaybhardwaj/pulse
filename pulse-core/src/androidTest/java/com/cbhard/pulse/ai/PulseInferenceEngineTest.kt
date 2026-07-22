package com.cbhard.pulse.ai

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PulseInferenceEngineTest {

    private lateinit var inferenceEngine: PulseInferenceEngine

    @Before
    fun setUp() {
        // Grab the Context of the test environment to access the assets folder
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Initialize our real inference engine
        inferenceEngine = PulseInferenceEngine(context)

        // Give the TFLite interpreter a tiny moment to load from disk
        Thread.sleep(500)
    }

    @Test
    fun testRecompositionAnomaly_OutputsCorrectFix() {
        // 1. Construct a mock JSON payload simulating a Compose meltdown
        val payload = JSONObject().apply {
            put("trigger", JSONObject().apply {
                put("type", "ExcessiveRecomposition")
                put("description", "CartButton spinning at 45 RPS. Lifecycle: Resumed.")
            })
            put("environment", JSONObject().apply { put("device_model", "TestDevice") })
        }

        // 2. Run inference
        val result = inferenceEngine.analyze(payload)

        // 3. Assert the model correctly identified the issue
        assertEquals("ExcessiveRecomposition", result.anomalyType)
        assertTrue("Confidence should be high", result.confidenceScore > 0.70f)
    }

    @Test
    fun testUIFreezeAnomaly_OutputsCorrectFix() {
        val payload = JSONObject().apply {
            put("trigger", JSONObject().apply {
                put("type", "UIFreeze")
                put("description", "SEVERE: Main thread blocked for 1200ms in DatabaseHelper.")
            })
        }

        val result = inferenceEngine.analyze(payload)

        assertEquals("UIFreeze", result.anomalyType)
        assertTrue("Confidence should be high", result.confidenceScore > 0.70f)
    }

    @Test
    fun testMemoryLeakAnomaly_OutputsCorrectFix() {
        val payload = JSONObject().apply {
            put("trigger", JSONObject().apply {
                put("type", "MemoryLeak")
                put("description", "UserProfileActivity retained in memory after 5 seconds. WeakReference survived.")
            })
        }

        val result = inferenceEngine.analyze(payload)

        assertEquals("MemoryLeak", result.anomalyType)
        assertTrue("Confidence should be high", result.confidenceScore > 0.70f)
    }

    @Test
    fun testGarbageData_HandlesGracefully() {
        // Pass completely unrelated JSON to ensure the model doesn't crash
        val payload = JSONObject().apply {
            put("random_data", "User clicked a button")
            put("timestamp", 123456789)
        }

        val result = inferenceEngine.analyze(payload)

        // It should still return a valid FixRecommendation object (even if it's a low-confidence guess)
        assertTrue(result.anomalyType.isNotEmpty())
    }
}