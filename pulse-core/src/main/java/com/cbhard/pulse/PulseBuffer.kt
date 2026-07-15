package com.cbhard.pulse

import android.util.Log

/**
 * A fixed-size, thread-safe circular buffer.
 * It holds the last N interactions to provide context when an anomaly occurs.
 */
internal class PulseBuffer(private val capacity: Int = 100) {

    private val queue = ArrayDeque<PulseEvent>(capacity)

    @Synchronized
    fun record(event: PulseEvent) {
        if (queue.size >= capacity) {
            queue.removeFirst() // Drop the oldest event (Time-travel window slides forward)
        }
        queue.addLast(event)

        // Optional: Log it just so we can see it working in V1
        Log.v("[PulseCore] PulseBuffer", "Recorded: $event")
    }

    /**
     * Extracts a frozen snapshot of the timeline to be sent to the AI engine.
     */
    @Synchronized
    fun extractTimeline(): List<PulseEvent> {
        return queue.toList()
    }

    /**
     * Clears all recorded events from the buffer.
     */
    @Synchronized
    fun clear() {
        queue.clear()
    }

    /**
     * Returns the number of events currently stored in the buffer.
     */
    val size: Int
        @Synchronized get() = queue.size
}