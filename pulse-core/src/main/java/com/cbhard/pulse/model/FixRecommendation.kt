package com.cbhard.pulse.model

data class FixRecommendation(
    val anomalyType: String,
    val rootCause: String,
    val suggestedFix: String,
    val confidenceScore: Float // 0.0 to 1.0
) {
    override fun toString(): String {
        return """
            💡 AI DIAGNOSTIC [Confidence: ${(confidenceScore * 100).toInt()}%]
            ------------------------------------------------
            🚨 Anomaly:   $anomalyType
            🔍 Root Cause: $rootCause
            🛠️ Suggested Fix:
            $suggestedFix
            ------------------------------------------------
        """.trimIndent()
    }
}