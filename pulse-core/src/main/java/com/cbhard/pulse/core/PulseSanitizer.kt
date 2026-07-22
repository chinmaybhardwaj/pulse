package com.cbhard.pulse.core

import java.util.regex.Pattern

internal object PulseSanitizer {

    // Pre-compile patterns for maximum performance
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    )
    
    // Matches 13-16 digit numbers that look like credit cards (with or without spaces/dashes)
    private val CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b(?:\\d[ -]*?){13,16}\\b"
    )
    
    // Matches standard Bearer auth tokens or JWTs
    private val AUTH_TOKEN_PATTERN = Pattern.compile(
        "(?i)bearer\\s+[A-Za-z0-9\\-\\._~\\+\\/]+=*"
    )
    
    // Matches explicit password assignments (e.g., password=mySecret123)
    private val PASSWORD_PATTERN = Pattern.compile(
        "(?i)(password|passwd|pwd)\\s*[:=]\\s*([^\\s,]+)"
    )

    /**
     * Scrubs all known PII formats from a given string.
     */
    fun sanitize(input: String): String {
        if (input.isEmpty()) return input

        var scrubbed = input
        
        scrubbed = EMAIL_PATTERN.matcher(scrubbed).replaceAll("[EMAIL_REDACTED]")
        scrubbed = CREDIT_CARD_PATTERN.matcher(scrubbed).replaceAll("[CARD_REDACTED]")
        scrubbed = AUTH_TOKEN_PATTERN.matcher(scrubbed).replaceAll("[TOKEN_REDACTED]")
        
        // For passwords, we keep the key but redact the value
        val passwordMatcher = PASSWORD_PATTERN.matcher(scrubbed)
        scrubbed = passwordMatcher.replaceAll("$1=[PASSWORD_REDACTED]")

        return scrubbed
    }
}