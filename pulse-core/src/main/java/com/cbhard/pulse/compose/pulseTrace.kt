package com.cbhard.pulse.compose

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.cbhard.pulse.core.PulseCore
import com.cbhard.pulse.core.PulseSafeguard

/**
 * A lightweight modifier that host developers can attach to complex screens.
 * It uses [SideEffect] which only fires AFTER a successful recomposition is committed.
 */
fun Modifier.pulseTrace(componentName: String): Modifier = composed {

    // If the circuit breaker is tripped, act like this modifier doesn't exist
    if (!PulseSafeguard.isActive) return@composed this

    return@composed PulseSafeguard.execute("[PulseCore] Compose") {
        // This block fires every time the Compose phase hits this node
        SideEffect {
            try {
                PulseCore.getInstance().composeMonitor.recordRecomposition(componentName)
            } catch (e: IllegalStateException) {
                // SDK not initialized yet, ignore safely.
            }
        }
        this // Return the wrapped modifier
    } ?: this // Fallback: If execute() returns null due to a crash, return the original unmodified Modifier
}