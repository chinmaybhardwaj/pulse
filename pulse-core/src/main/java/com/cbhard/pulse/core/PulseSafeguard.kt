package com.cbhard.pulse.core

import android.util.Log

internal object PulseSafeguard {
    
    // The Circuit Breaker flag
    @Volatile
    var isActive: Boolean = true
        private set

    /**
     * Executes the block safely. If an exception occurs, it logs internally 
     * and prevents the crash from reaching the host app.
     */
    inline fun <T> execute(tag: String = "[PulseCore] Internal", block: () -> T): T? {
        if (!isActive) return null

        return try {
            block()
        } catch (e: VirtualMachineError) {
            // Fatal JVM errors (like OutOfMemoryError). 
            // Trip the circuit breaker immediately.
            Log.e(tag, "FATAL SDK ERROR: Tripping circuit breaker. SDK disabled.", e)
            isActive = false
            null
        } catch (e: Throwable) {
            // Standard exceptions. Log silently, keep the host app alive.
            Log.e(tag, "SDK Exception caught and suppressed.", e)
            null
        }
    }

    // Optional manual kill switch for the host app
    fun disable() {
        isActive = false
    }
}