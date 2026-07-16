# Protect the App Startup Initializer so the OS can find it
-keep class com.cbhard.pulse.PulseInitializer { *; }

# Protect the AI Models from being obfuscated (renamed to a, b, c)
# so the JSON serialization doesn't break
-keep class com.cbhard.pulse.PulseEvent** { *; }
-keep class com.cbhard.pulse.FixRecommendation { *; }

# Keep the public modifier extension so host apps can use it
-keepclassmembers class com.cbhard.pulse.PulseTraceKt {
    public static *** pulseTrace(...);
}