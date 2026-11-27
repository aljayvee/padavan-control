package com.example.padavancontrol.ui.viewmodels

/**
 * Small helper for adaptive polling. Successful polls use the base delay;
 * consecutive failures back off exponentially up to [maxDelayMs].
 */
internal class PollingBackoff(
    private val baseDelayMs: Long,
    private val maxDelayMs: Long = 30_000L
) {
    private var consecutiveFailures = 0

    fun onSuccess() {
        consecutiveFailures = 0
    }

    fun onFailure() {
        if (consecutiveFailures < 8) {
            consecutiveFailures++
        }
    }

    fun nextDelayMs(): Long {
        if (consecutiveFailures == 0) return baseDelayMs
        val multiplier = 1L shl consecutiveFailures.coerceAtMost(5)
        return (baseDelayMs * multiplier).coerceAtMost(maxDelayMs)
    }
}
