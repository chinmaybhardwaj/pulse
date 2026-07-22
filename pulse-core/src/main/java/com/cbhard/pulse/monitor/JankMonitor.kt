package com.cbhard.pulse.monitor

import android.util.Log
import android.view.Choreographer
import com.cbhard.pulse.ai.AiPayloadBuilder
import com.cbhard.pulse.buffer.PulseBuffer
import com.cbhard.pulse.core.PulseSafeguard
import com.cbhard.pulse.model.PulseEvent
import java.util.concurrent.TimeUnit

internal class JankMonitor(
    private val buffer: PulseBuffer,
    private val aiPayloadBuilder: AiPayloadBuilder
) : Choreographer.FrameCallback {

    private var isMonitoring = false
    private var lastFrameTimeNanos: Long = 0

    // 16.6ms for 60fps, 300ms for a severe freeze
    private val frameBudgetNanos = TimeUnit.MILLISECONDS.toNanos(17)
    private val severeFreezeNanos = TimeUnit.MILLISECONDS.toNanos(300)

    fun start() {
        if (!isMonitoring) {
            isMonitoring = true
            lastFrameTimeNanos = System.nanoTime()
            Choreographer.getInstance().postFrameCallback(this)
            Log.v("[PulseCore]", "Jank Monitor started.")
        }
    }

    fun stop() {
        if (isMonitoring) {
            isMonitoring = false
            Choreographer.getInstance().removeFrameCallback(this)
            Log.v("[PulseCore]", "Jank Monitor stopped.")
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!isMonitoring) return
        PulseSafeguard.execute("[PulseCore] Jank") {
            if (lastFrameTimeNanos != 0L) {
                val frameDuration = frameTimeNanos - lastFrameTimeNanos

                if (frameDuration > severeFreezeNanos) {
                    val freezeMs = TimeUnit.NANOSECONDS.toMillis(frameDuration)
                    Log.e(
                        "[PulseCore]",
                        "🥶 SEVERE UI FREEZE: Main thread blocked for ${freezeMs}ms!"
                    )
                    val anomaly = PulseEvent.Anomaly(
                        "UIFreeze",
                        "Main thread blocked for ${freezeMs}ms"
                    )
                    buffer.record(anomaly)
                    // Trigger our AI payload builder
                    aiPayloadBuilder.generateReport(anomaly, buffer.extractTimeline())
                } else if (frameDuration > frameBudgetNanos) {
                    // Calculate how many frames we missed
                    val droppedFrames = (frameDuration / frameBudgetNanos).toInt()
                    if (droppedFrames > 3) { // Only log noticeable stutters to avoid buffer spam
                        Log.w("[PulseCore]", "⚠️ JANK: Dropped $droppedFrames frames.")
                        buffer.record(
                            PulseEvent.Metric(
                                "DroppedFrames",
                                droppedFrames.toLong(),
                                "frames"
                            )
                        )
                    }
                }
            }

            // 1. Update the last frame time
            lastFrameTimeNanos = frameTimeNanos

            // 2. Queue up the next frame check
            Choreographer.getInstance().postFrameCallback(this)
        }
    }
}