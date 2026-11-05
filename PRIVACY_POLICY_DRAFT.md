# Privacy Policy — Wavi Companion

**Last Updated:** November 2026

## Introduction

Wavi Companion ("the App") is an independent Android application that provides access to WhatsApp Web within a native mobile interface. This privacy policy explains how the App handles user data.

**Wavi Companion is not affiliated with, endorsed by, or connected to WhatsApp LLC or Meta Platforms, Inc.**

## Data Collection

**Wavi Companion does not collect, store, transmit, or process any personal data.**

Specifically, the App does not:
- Collect your name, email address, phone number, or other personal identifiers.
- Read, store, or transmit your WhatsApp messages, contacts, or media.
- Extract or store WhatsApp authentication cookies or session tokens.
- Include third-party analytics, advertising, or tracking frameworks.
- Send any data to external servers operated by the developer.

## How the App Works

The App embeds an Android WebView that loads the official WhatsApp Web website (`https://web.whatsapp.com`). All communication between your device and WhatsApp servers occurs directly through this WebView, using WhatsApp's own encrypted protocols.

The App does not intercept, modify, or inspect the data exchanged between the WebView and WhatsApp servers.

## Local Data Storage

The following data is stored locally on your device only:

- **WebView Data:** The Android WebView stores cookies, local storage, and cached content required by WhatsApp Web to maintain your linked session. This data is managed by the system WebView and is not accessible to the App's own code.
- **App Preferences:** Theme selection, onboarding completion status, and display settings are stored using Android DataStore. These contain no personal information.

All locally stored data can be cleared through the App's Settings screen or by clearing the App's data in Android system settings.

## Permissions

The App may request the following permissions:

| Permission | Purpose |
|-----------|---------|
| Internet | Required to load WhatsApp Web. |
| Camera | Used by WhatsApp Web for QR code scanning during device linking. |
| Microphone | Used by WhatsApp Web for voice messages and calls. |

No permission is used for any purpose other than enabling WhatsApp Web functionality within the App.

## Third-Party Services

The App loads content exclusively from `whatsapp.com` and its subdomains. The developer does not control WhatsApp's data practices. For information about how WhatsApp handles your data, refer to [WhatsApp's Privacy Policy](https://www.whatsapp.com/legal/privacy-policy).

## Children's Privacy

The App is not directed at children under 13. It does not knowingly collect any data from children.

## Security

- All network communication is encrypted (HTTPS only; cleartext traffic is disabled).
- SSL certificate errors are never bypassed.
- No authentication credentials are collected or stored by the App.
- WebView navigation is restricted to trusted WhatsApp domains.

## Changes to This Policy

This privacy policy may be updated from time to time. Changes will be reflected in the "Last Updated" date above. Continued use of the App constitutes acceptance of the updated policy.

## Contact

For questions about this privacy policy, contact:

**[Developer Name]**
**[Developer Email]**

---

*This document is a draft template. It should be reviewed by a legal professional before use in any commercial or public distribution context.*
