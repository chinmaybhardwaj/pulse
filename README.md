# ⚡ Pulse SDK

**Zero-Overhead Edge AI Performance Profiler for Android**

[![Release](https://jitpack.io/v/chinmaybhardwaj/pulse.svg)](https://jitpack.io/#chinmaybhardwaj/pulse/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![TFLite](https://img.shields.io/badge/TFLite-INT8%20Quantized-orange.svg)]()

Pulse is an autonomous, on-device diagnostic SDK that detects UI freezes, memory leaks, and Jetpack Compose state loops in real time. Instead of generating massive `.hprof` files or blocking the main thread with heavy telemetry, Pulse maintains a lightweight, time-traveling memory buffer and uses a custom **15 KB INT8 Quantized TensorFlow Lite model** to analyze anomalies entirely offline.

---

## ✨ Features

- **Zero-Code Initialization**  
  Hooks into `androidx.startup`. No `Pulse.init()` call is required in your `Application` class.

- **Compose Meltdown Detection**  
  Tracks Recompositions Per Second (RPS) via a lightweight custom `Modifier` to detect infinite state loops.

- **Silent Memory Leak Analysis**  
  Uses background `WeakReference` polling instead of heap dumps to identify retained `Activity` instances without freezing the UI.

- **Jank & Frame Tracking**  
  Hooks directly into the `Choreographer` to detect dropped frames exceeding 16 ms.

- **On-Device AI Diagnostics**  
  Telemetry is vectorized and analyzed by a local TensorFlow Lite model to generate plain-text root cause analysis and actionable fix recommendations.

---

## 📦 Installation

Pulse is distributed via **JitPack**.

### 1. Add the JitPack repository

In your root `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add the dependency

In your app-level `build.gradle.kts`:

```kotlin
dependencies {
    // Replace with the latest release version
    implementation("com.github.YourUsername:pulse-core:1.0.0")
}
```

---

## 🚀 Usage

### Automatic Monitoring

For standard Android lifecycle and frame tracking, **no setup is required**.

Pulse automatically initializes when your app starts and silently begins monitoring:

- Activity lifecycle events
- Choreographer frame timings
- UI jank
- Memory retention

### Jetpack Compose Tracking

To monitor recomposition hotspots, attach the `pulseTrace` modifier to the composables you want to profile.

```kotlin
@Composable
fun HeavyDashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pulseTrace("Dashboard_Root_Column")
    ) {
        // Your UI content
    }
}
```

---

## 🧠 Under the Hood (Edge AI)

When Pulse detects a critical anomaly (for example, the UI thread being blocked for more than **300 ms**), it retrieves the most recent **150 timeline events** from its synchronized `ArrayDeque`.

The captured context is serialized, tokenized locally, and passed into the bundled TensorFlow Lite classifier. The model generates a `FixRecommendation`, which is written to Logcat under the **`PulseCore_Diagnostics`** tag.

```text
E/PulseCore_Diagnostics:
FixRecommendation(
    anomalyType = ExcessiveRecomposition,
    rootCause = A state variable is being read and modified within the same Compose phase.,
    suggestedFix = Move state mutations inside a SideEffect block (e.g., LaunchedEffect).,
    confidenceScore = 0.984
)
```
### 🔒 Enterprise-Grade Data Privacy
Pulse is designed for strict GDPR & CCPA compliance:
* **100% Offline:** Telemetry and AI inference occur locally. Pulse makes zero network calls and has no backend dependency.
* **PII Sanitization:** A built-in Regex interceptor actively strips emails, auth tokens, credit cards, and password variables from stack traces and Compose nodes before they are processed or logged.