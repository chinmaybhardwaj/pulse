# Protect the App Startup Initializer so the OS can find it
-keep class com.cbhard.pulse.core.PulseInitializer { *; }

# Protect the AI Models from being obfuscated (renamed to a, b, c)
# so the JSON serialization doesn't break
-keep class com.cbhard.pulse.PulseEvent** { *; }
-keep class com.cbhard.pulse.model.FixRecommendation { *; }

# Keep the public modifier extension so host apps can use it
-keepclassmembers class com.cbhard.pulse.compose.PulseTraceKt {
    public static *** pulseTrace(...);
}

# 1. Protect TensorFlow Lite Native Bindings
# TFLite relies on C++ JNI calls. If R8 renames the Java wrappers, the C++ code will fail to link.
-keep class org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# 2. Protect the Pulse Internal Models
# If we use reflection or JSON mapping anywhere in the payload builder,
# R8 renaming will break the JSON keys.
-keep class com.cbhard.pulse.model.** { *; }

# 3. Suppress TFLite warnings
# Prevents the host app's build from failing due to missing TFLite metadata classes
-dontwarn org.tensorflow.lite.**