package com.wavicompanion.app.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstantsTest {

    @Test
    fun `WHATSAPP_WEB_URL uses HTTPS`() {
        assertTrue(Constants.WHATSAPP_WEB_URL.startsWith("https://"))
    }

    @Test
    fun `WHATSAPP_WEB_URL points to correct domain`() {
        assertEquals("https://web.whatsapp.com", Constants.WHATSAPP_WEB_URL)
    }

    @Test
    fun `ALLOWED_HOSTS contains essential WhatsApp domains`() {
        assertTrue(Constants.ALLOWED_HOSTS.contains("web.whatsapp.com"))
        assertTrue(Constants.ALLOWED_HOSTS.contains("whatsapp.com"))
        assertTrue(Constants.ALLOWED_HOSTS.contains("www.whatsapp.com"))
    }

    @Test
    fun `ALLOWED_HOSTS contains media domains`() {
        assertTrue(Constants.ALLOWED_HOSTS.contains("static.whatsapp.net"))
        assertTrue(Constants.ALLOWED_HOSTS.contains("mmg.whatsapp.net"))
        assertTrue(Constants.ALLOWED_HOSTS.contains("media.whatsapp.net"))
        assertTrue(Constants.ALLOWED_HOSTS.contains("pps.whatsapp.net"))
    }

    @Test
    fun `ALLOWED_HOSTS does not contain non-WhatsApp domains`() {
        assertTrue(Constants.ALLOWED_HOSTS.none { it.contains("google") })
        assertTrue(Constants.ALLOWED_HOSTS.none { it.contains("facebook") })
    }

    @Test
    fun `VERSION follows semver format`() {
        assertTrue(Constants.VERSION.matches(Regex("\\d+\\.\\d+\\.\\d+")))
    }
}
