package com.example.padavancontrol

import com.example.padavancontrol.network.PadavanResponseParser
import org.junit.Assert.assertEquals
import org.junit.Test

class WanStatusParserTest {

    @Test
    fun parsesWanLinkUptimeAndConnectionDetails() {
        val html = """
            function wanlink_status() { return 0;}
            function wanlink_uptime() { return 123456;}
            function wanlink_dltime() { return 0;}
            function wanlink_type() { return 'Automatic IP';}
            function wanlink_ip4_wan() { return '203.0.113.5';}
            function wanlink_gw4_wan() { return '203.0.113.1';}
            function wanlink_dns() { return '1.1.1.1<br/>8.8.8.8';}
        """.trimIndent()

        val status = PadavanResponseParser.parseWanStatus(html)

        requireNotNull(status)
        assertEquals(true, status.isConnected)
        assertEquals(123456L, status.uptime)
        assertEquals("203.0.113.5", status.wanIp)
        assertEquals("203.0.113.1", status.gateway)
        assertEquals("1.1.1.1", status.dns1)
        assertEquals("8.8.8.8", status.dns2)
    }

    @Test
    fun normalizesMissingDnsServer() {
        val html = """
            function wanlink_status() { return 0;}
            function wanlink_uptime() { return 60;}
            function wanlink_type() { return 'DHCP';}
            function wanlink_ip4_wan() { return '192.168.1.2';}
            function wanlink_gw4_wan() { return '192.168.1.1';}
            function wanlink_dns() { return '---';}
        """.trimIndent()

        val status = PadavanResponseParser.parseWanStatus(html)

        requireNotNull(status)
        assertEquals("0.0.0.0", status.dns1)
        assertEquals("0.0.0.0", status.dns2)
    }
}
