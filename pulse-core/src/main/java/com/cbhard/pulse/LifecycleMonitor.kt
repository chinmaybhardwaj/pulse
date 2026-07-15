package com.cbhard.pulse

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log

internal class LifecycleMonitor(
    private val buffer: PulseBuffer,
    private val leakAnalyzer: LeakAnalyzer,
    private val jankMonitor: JankMonitor
) : Application.ActivityLifecycleCallbacks {

    // Capture the exact moment our process started via the Initializer
    private val processStartTime = SystemClock.uptimeMillis()
    private var isFirstActivityCreated = false

    // Track how many activities are visible
    private var resumedActivityCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        val activityName = activity.javaClass.simpleName
        buffer.record(PulseEvent.Lifecycle(activityName, "Created"))
        if (!isFirstActivityCreated) {
            isFirstActivityCreated = true
            val ttid = SystemClock.uptimeMillis() - processStartTime
            buffer.record(PulseEvent.Metric("TimeToInitialDisplay", ttid, "ms"))
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        val activityName = activity.javaClass.simpleName
        Log.d("[PulseCore]", "$activityName destroyed. Queuing for leak detection...")
        buffer.record(PulseEvent.Lifecycle(activityName, "Destroyed"))
        // TODO: Memory Leak Trigger goes here
        leakAnalyzer.watch(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        buffer.record(PulseEvent.Lifecycle(activity.javaClass.simpleName, "Started"))
    }

    override fun onActivityResumed(activity: Activity) {
        buffer.record(PulseEvent.Lifecycle(activity.javaClass.simpleName, "Resumed"))
        if (resumedActivityCount == 0) {
            // App just came to the foreground! Start measuring frames.
            jankMonitor.start()
        }
        resumedActivityCount++
    }

    override fun onActivityPaused(activity: Activity) {
        buffer.record(PulseEvent.Lifecycle(activity.javaClass.simpleName, "Paused"))
        resumedActivityCount--
        if (resumedActivityCount == 0) {
            // App went to the background. Stop measuring frames to save battery.
            jankMonitor.stop()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        buffer.record(PulseEvent.Lifecycle(activity.javaClass.simpleName, "Stopped"))
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        buffer.record(PulseEvent.Lifecycle(activity.javaClass.simpleName, "SaveInstanceState"))
    }
}