package com.cbhard.pulse.model

/**
 * A strict schema for system events. 
 * This structure is designed to be easily serialized into AI-digestible tokens.
 */
sealed class PulseEvent(val timestamp: Long = System.currentTimeMillis()) {

    // Tracks UI movement
    data class Lifecycle(val component: String, val state: String) : PulseEvent()

    // Tracks performance numbers (e.g., Startup time, Memory spikes)
    data class Metric(val name: String, val value: Long, val unit: String) : PulseEvent()

    // Tracks bad behavior (Leaks, Jank, Exceptions)
    data class Anomaly(val type: String, val description: String) : PulseEvent()

    // Format for easy logging and future JSON serialization
    override fun toString(): String {
        return "[${timestamp}] ${this.javaClass.simpleName}: " + when (this) {
            is Lifecycle -> "$component -> $state"
            is Metric -> "$name = $value $unit"
            is Anomaly -> "$type - $description"
        }
    }
}