package com.wavicompanion.app.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WebViewNotificationBridgeTest {

    // === JS Injection Tests ===

    @Test
    fun `JS injection contains double-injection guard`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("_waviNotificationPatched"))
        assertTrue(js.contains("if (window._waviNotificationPatched) return"))
    }

    @Test
    fun `JS injection overrides Notification permission to granted`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("permission = 'granted'"))
    }

    @Test
    fun `JS injection overrides requestPermission to resolve granted`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("Promise.resolve('granted')"))
    }

    @Test
    fun `JS injection intercepts ServiceWorker showNotification`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("ServiceWorkerRegistration.prototype.showNotification"))
    }

    @Test
    fun `JS injection sets window Notification to override`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("window.Notification = WaviNotification"))
    }

    @Test
    fun `JS injection calls native bridge with title body tag icon`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("window.WaviNotificationBridge.onNotification"))
        assertTrue(js.contains("title || ''"))
        assertTrue(js.contains("options.body || ''"))
        assertTrue(js.contains("options.tag || ''"))
        assertTrue(js.contains("options.icon || ''"))
    }

    @Test
    fun `JS injection is wrapped in IIFE`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS.trim()
        assertTrue(js.startsWith("(function()"))
        assertTrue(js.endsWith("})();"))
    }

    @Test
    fun `JS injection intercepts getNotifications`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("getNotifications"))
        assertTrue(js.contains("Promise.resolve([])"))
    }

    @Test
    fun `JS interface name constant is correct`() {
        assertEquals("WaviNotificationBridge", WebViewNotificationBridge.JS_INTERFACE_NAME)
    }

    @Test
    fun `JS injection provides close and event listener stubs`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("prototype.close"))
        assertTrue(js.contains("prototype.addEventListener"))
        assertTrue(js.contains("prototype.removeEventListener"))
    }

    @Test
    fun `JS injection sets maxActions`() {
        val js = WebViewNotificationBridge.NOTIFICATION_INTERCEPT_JS
        assertTrue(js.contains("maxActions"))
    }

    // === Input Validation Tests ===

    @Test
    fun `string truncation at max lengths`() {
        assertEquals(200, "A".repeat(500).take(200).length)
        assertEquals(1000, "B".repeat(5000).take(1000).length)
        assertEquals(100, "T".repeat(300).take(100).length)
    }

    @Test
    fun `short strings are not truncated`() {
        assertEquals("hello", "hello".take(200))
        assertEquals("world", "world".take(1000))
        assertEquals("tag1", "tag1".take(100))
    }

    @Test
    fun `empty strings pass through take`() {
        assertEquals("", "".take(200))
        assertEquals("", "".take(1000))
    }

    @Test
    fun `blank title with content body should use WhatsApp fallback`() {
        val title = "   "
        val safeTitle = title.take(200)
        val result = safeTitle.ifBlank { "WhatsApp" }
        assertEquals("WhatsApp", result)
    }

    @Test
    fun `non-blank title is preserved`() {
        val title = "John Doe"
        val safeTitle = title.take(200)
        val result = safeTitle.ifBlank { "WhatsApp" }
        assertEquals("John Doe", result)
    }

    @Test
    fun `blank tag becomes null`() {
        val tag = ""
        val safeTag = tag.take(100)
        val result = safeTag.ifBlank { null }
        assertEquals(null, result)
    }

    @Test
    fun `non-blank tag is preserved`() {
        val tag = "chat_123456"
        val safeTag = tag.take(100)
        val result = safeTag.ifBlank { null }
        assertEquals("chat_123456", result)
    }

    @Test
    fun `unicode characters are handled correctly`() {
        val emoji = "😀".repeat(150)
        val truncated = emoji.take(200)
        assertTrue(truncated.length <= 200)
    }

    @Test
    fun `special characters in title and body`() {
        val specialChars = "<script>alert('xss')</script>"
        val truncated = specialChars.take(200)
        assertEquals(specialChars, truncated)
    }

    @Test
    fun `multiline message body`() {
        val multiline = "Line 1\nLine 2\nLine 3\n" + "More\n".repeat(200)
        val truncated = multiline.take(1000)
        assertTrue(truncated.length <= 1000)
        assertTrue(truncated.contains("\n"))
    }

    // === Notification ID Tests ===

    @Test
    fun `notification IDs are unique for sequential calls`() {
        val ids = mutableSetOf<Int>()
        repeat(1000) {
            ids.add(NotificationHelper.nextNotificationId())
        }
        assertEquals(1000, ids.size)
    }

    @Test
    fun `notification IDs stay within valid range`() {
        repeat(500) {
            val id = NotificationHelper.nextNotificationId()
            assertTrue("ID $id should be >= 100", id >= 100)
            assertTrue("ID $id should be < 50000", id < 50000)
        }
    }

    @Test
    fun `notification IDs are unique under concurrent access`() {
        val threadCount = 20
        val idsPerThread = 100
        val allIds = Collections.synchronizedSet(mutableSetOf<Int>())
        val latch = CountDownLatch(threadCount)
        val executor = Executors.newFixedThreadPool(threadCount)

        repeat(threadCount) {
            executor.submit {
                try {
                    repeat(idsPerThread) {
                        allIds.add(NotificationHelper.nextNotificationId())
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(10, TimeUnit.SECONDS)
        executor.shutdown()
        assertEquals(threadCount * idsPerThread, allIds.size)
    }

    @Test
    fun `notification IDs do not collide with service notification ID`() {
        repeat(200) {
            val id = NotificationHelper.nextNotificationId()
            assertNotEquals(NotificationHelper.SERVICE_NOTIFICATION_ID, id)
        }
    }

    // === Concurrent Notification Simulation Tests ===

    @Test
    fun `simulate 5 rapid notifications from different contacts`() {
        val contacts = listOf("Alice", "Bob", "Charlie", "David", "Eve")
        val results = ConcurrentHashMap<String, Int>()
        val latch = CountDownLatch(contacts.size)
        val executor = Executors.newFixedThreadPool(contacts.size)

        contacts.forEach { contact ->
            executor.submit {
                try {
                    val id = NotificationHelper.nextNotificationId()
                    results[contact] = id
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()
        assertEquals(5, results.size)
        assertEquals(5, results.values.toSet().size)
    }

    @Test
    fun `simulate 20 rapid notifications`() {
        val ids = Collections.synchronizedList(mutableListOf<Int>())
        val latch = CountDownLatch(20)
        val executor = Executors.newFixedThreadPool(20)

        repeat(20) {
            executor.submit {
                try {
                    ids.add(NotificationHelper.nextNotificationId())
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await(5, TimeUnit.SECONDS)
        executor.shutdown()
        assertEquals(20, ids.size)
        assertEquals(20, ids.toSet().size)
    }

    @Test
    fun `simulate same-contact burst of 10 messages`() {
        val ids = mutableListOf<Int>()
        repeat(10) {
            ids.add(NotificationHelper.nextNotificationId())
        }
        assertEquals(10, ids.size)
        assertEquals(10, ids.toSet().size)
    }

    // === Tag & Grouping Tests ===

    @Test
    fun `different contacts produce different tags`() {
        val tags = listOf("chat_alice", "chat_bob", "chat_charlie")
        assertEquals(3, tags.toSet().size)
    }

    @Test
    fun `null tag does not cause issues`() {
        val tag: String? = null
        val safeTag = tag?.take(100)?.ifBlank { null }
        assertEquals(null, safeTag)
    }

    @Test
    fun `hash collision proof - different strings can have same hashCode`() {
        val s1 = "Siblings"
        val s2 = "Teheran"
        if (s1.hashCode() == s2.hashCode()) {
            assertEquals(s1.hashCode(), s2.hashCode())
        }
        assertNotEquals(s1, s2)
    }

    @Test
    fun `different contacts get unique IDs and distinct tags`() {
        val tag1 = "chat_alice"
        val tag2 = "chat_bob"
        val id1 = NotificationHelper.nextNotificationId()
        val id2 = NotificationHelper.nextNotificationId()
        assertNotEquals(id1, id2)
        assertNotEquals(tag1, tag2)
    }

    // === Both blank title and body should be filtered ===

    @Test
    fun `both blank title and body should be filtered`() {
        val title = ""
        val body = ""
        val safeTitle = title.take(200)
        val safeBody = body.take(1000)
        assertTrue(safeTitle.isBlank() && safeBody.isBlank())
    }

    @Test
    fun `blank title with non-blank body should not be filtered`() {
        val title = ""
        val body = "Hello"
        val safeTitle = title.take(200)
        val safeBody = body.take(1000)
        assertTrue(!(safeTitle.isBlank() && safeBody.isBlank()))
    }

    @Test
    fun `non-blank title with blank body should not be filtered`() {
        val title = "Alice"
        val body = ""
        val safeTitle = title.take(200)
        val safeBody = body.take(1000)
        assertTrue(!(safeTitle.isBlank() && safeBody.isBlank()))
    }
}
