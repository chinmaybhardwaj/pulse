package com.cbhard.pulse.compose

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.cbhard.pulse.core.PulseCore

/**
 * A lightweight modifier that host developers can attach to complex screens.
 * It uses [SideEffect] which only fires AFTER a successful recomposition is committed.
 */
fun Modifier.pulseTrace(componentName: String): Modifier = composed {
    
    // This block fires every time the Compose phase hits this node
    SideEffect {
        try {
            PulseCore.getInstance().composeMonitor.recordRecomposition(componentName)
        } catch (e: IllegalStateException) {
            // SDK not initialized yet, ignore safely.
        }
    }
    
    // Return the unmodified chain
    this
}