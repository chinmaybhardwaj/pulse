# Pulse

**Understand your app, not just its metrics.**

Pulse is an Android monitoring SDK prototype that records a compact timeline of app lifecycle events and detects two high-impact runtime problems:

- noticeable UI jank and severe main-thread freezes;
- activities that remain retained after destruction.

The repository also includes a Compose sample app that intentionally creates both failures, making it useful for developing and validating the SDK.

## Project structure

| Module | Purpose |
| --- | --- |
| `pulse-core` | The monitoring library. It auto-initializes with AndroidX Startup and observes all host-app activities. |
| `app` | A demo application that depends on `pulse-core` and provides controls to trigger jank and an activity leak. |

## What Pulse records

`pulse-core` maintains a thread-safe circular buffer containing the latest 150 `PulseEvent`s. Events have a timestamp and one of three schemas:

- `Lifecycle` — activity creation, start, resume, pause, stop, save-state, and destruction;
- `Metric` — time to initial display and detected dropped frames;
- `Anomaly` — severe UI freezes and suspected activity memory leaks.

The buffer is internal to the library in the current prototype. Events are emitted to Logcat under tags such as `[PulseCore]` and `[PulseCore] PulseBuffer`.

## How monitoring works

Pulse initializes automatically through `androidx.startup.InitializationProvider`; no setup code is required in the sample app.

When the first activity is created, Pulse records `TimeToInitialDisplay`. While one or more activities are resumed, `JankMonitor` uses `Choreographer` callbacks to measure frame gaps:

- gaps over 17 ms that imply more than three missed frames are recorded as `DroppedFrames`;
- gaps over 300 ms are recorded as a `UIFreeze` anomaly.

When an activity is destroyed, `LeakAnalyzer` keeps only a `WeakReference`, waits five seconds on a background thread, requests garbage collection, and reports a `MemoryLeak` anomaly if the activity is still reachable.

> Leak detection here is intentionally lightweight and heuristic-based. A retained activity is a strong signal worth investigating, not a complete heap analysis.

## Run the demo

### Requirements

- Android Studio with an Android SDK supporting API 37
- JDK 17

### Android Studio

1. Open this directory in Android Studio.
2. Allow Gradle sync to complete.
3. Select the `app` configuration and run it on a device or emulator.
4. Filter Logcat for `PulseCore`.

### Command line

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

## Exercise the monitors

1. In the demo app, tap **Navigate to SecondActivity**.
2. Tap **Simulate Heavy UI Jank** to block the main thread for 600 ms and generate a UI-freeze event.
3. Tap **Finish Activity (Trigger Leak)**. `SecondActivity` intentionally stores itself in a static list, so Pulse should report a suspected leak roughly five seconds later.

The first screen’s **Finish Activity** control lets you exercise ordinary activity teardown as well.

## Development

Run the available unit and instrumentation test tasks with:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

Key implementation files:

- `pulse-core/src/main/java/com/cbhard/pulse/PulseCore.kt` — singleton SDK setup
- `pulse-core/src/main/java/com/cbhard/pulse/LifecycleMonitor.kt` — lifecycle and startup-time monitoring
- `pulse-core/src/main/java/com/cbhard/pulse/JankMonitor.kt` — frame timing monitor
- `pulse-core/src/main/java/com/cbhard/pulse/LeakAnalyzer.kt` — retained-activity check
- `pulse-core/src/main/java/com/cbhard/pulse/PulseBuffer.kt` — recent-event storage

## Current scope

Pulse is an early SDK prototype. The event model is designed to support future serialization and downstream analysis, but this revision does not yet expose a public event-export API, send data to a backend, or perform heap-path analysis.
