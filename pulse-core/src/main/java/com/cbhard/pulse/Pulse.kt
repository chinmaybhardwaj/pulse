package com.cbhard.pulse

import android.util.Log
import com.cbhard.pulse.core.PulseCore
import com.cbhard.pulse.core.PulseSafeguard

/**
 * Public API surface for the Pulse SDK.
 */
object Pulse {

    /**
     * Runtime Kill Switch. 
     * If set to false, Pulse will immediately unregister all system hooks, 
     * clear its memory buffers, and remain dormant for the rest of the session.
     */
    fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            Log.w("[PulseCore]", "Remote Kill Switch triggered. Shutting down Pulse SDK.")
            PulseSafeguard.disable()
            PulseCore.getInstance().shutdown()
        }
    }
}