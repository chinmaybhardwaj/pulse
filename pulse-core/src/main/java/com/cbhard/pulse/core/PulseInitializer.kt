package com.cbhard.pulse.core

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class PulseInitializer : Initializer<PulseCore> {

    override fun create(context: Context): PulseCore {
        // Safe cast to Application context to prevent memory leaks in the SDK itself
        val application = context.applicationContext as Application
        return PulseCore.initialize(application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}