package com.cbhard.pulseBenchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FrameTimingBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkScrollPerformance() {
        benchmarkRule.measureRepeated(
            packageName = "com.cbhard.pulseapp",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()

            // Find a scrollable list in your sample app and scroll it
            val list = device.findObject(By.scrollable(true))
            if (list != null) {
                list.setGestureMargin(device.displayWidth / 5)
                list.scroll(Direction.DOWN, 1f)
            }
        }
    }
}