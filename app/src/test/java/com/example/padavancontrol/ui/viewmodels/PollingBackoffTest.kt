package com.example.padavancontrol.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Test

class PollingBackoffTest {

    @Test
    fun `uses base delay while polls succeed`() {
        val backoff = PollingBackoff(baseDelayMs = 1000L, maxDelayMs = 30_000L)

        assertEquals(1000L, backoff.nextDelayMs())

        backoff.onSuccess()
        assertEquals(1000L, backoff.nextDelayMs())
    }

    @Test
    fun `backs off exponentially after repeated failures`() {
        val backoff = PollingBackoff(baseDelayMs = 1000L, maxDelayMs = 30_000L)

        backoff.onFailure()
        assertEquals(2000L, backoff.nextDelayMs())

        backoff.onFailure()
        assertEquals(4000L, backoff.nextDelayMs())

        backoff.onFailure()
        assertEquals(8000L, backoff.nextDelayMs())
    }

    @Test
    fun `caps delay at maximum and resets after success`() {
        val backoff = PollingBackoff(baseDelayMs = 1000L, maxDelayMs = 10_000L)

        repeat(10) { backoff.onFailure() }
        assertEquals(10_000L, backoff.nextDelayMs())

        backoff.onSuccess()
        assertEquals(1000L, backoff.nextDelayMs())
    }
}
