package com.wavicompanion.app.core.common

object Constants {
    const val WHATSAPP_WEB_URL = "https://web.whatsapp.com"
    const val APP_NAME = "Wavi Companion"
    const val VERSION = "1.0.0"

    val ALLOWED_HOSTS = setOf(
        "web.whatsapp.com",
        "whatsapp.com",
        "www.whatsapp.com",
        "static.whatsapp.net",
        "mmg.whatsapp.net",
        "media.whatsapp.net",
        "pps.whatsapp.net",
    )

    const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; SM-A528B) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.6099.230 Mobile Safari/537.36"
}
