package com.wavicompanion.app.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NotificationHelperTest {

    @Test
    fun `channel IDs are distinct`() {
        assertNotEquals(NotificationHelper.CHANNEL_MESSAGES, NotificationHelper.CHANNEL_SERVICE)
    }

    @Test
    fun `service notification ID is positive`() {
        assert(NotificationHelper.SERVICE_NOTIFICATION_ID > 0)
    }

    @Test
    fun `channel messages ID is correct`() {
        assertEquals("wavi_messages", NotificationHelper.CHANNEL_MESSAGES)
    }

    @Test
    fun `channel service ID is correct`() {
        assertEquals("wavi_service", NotificationHelper.CHANNEL_SERVICE)
    }
}
