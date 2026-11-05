# Google Play Store Checklist

## Pre-Submission

### Legal & Policy Review
- [ ] Review WhatsApp Terms of Service for permitted third-party integrations
- [ ] Review Google Play Developer Program Policies
- [ ] Confirm the app does not violate the Impersonation policy
- [ ] Ensure no WhatsApp trademarks are used in the app name or icon in a misleading way
- [ ] Prepare a clear disclaimer that the app is not affiliated with WhatsApp or Meta
- [ ] Review local privacy laws (GDPR, CCPA, etc.)

### App Content
- [ ] App title: "Wavi Companion" (no "WhatsApp" in the title)
- [ ] Short description (80 characters max)
- [ ] Full description (4000 characters max)
- [ ] App category: Communication or Productivity
- [ ] Content rating questionnaire completed
- [ ] Privacy policy URL published and accessible

### Visual Assets
- [ ] App icon (512x512 PNG, 32-bit, no alpha)
- [ ] Feature graphic (1024x500 PNG or JPEG)
- [ ] Phone screenshots (minimum 2, recommended 4-8)
- [ ] 7-inch tablet screenshots (if supporting tablets)
- [ ] 10-inch tablet screenshots (if supporting tablets)

### Technical Requirements
- [ ] Target API level meets Google Play's current minimum (API 34+)
- [ ] 64-bit native libraries included (if applicable)
- [ ] App signed with a release key
- [ ] AAB (Android App Bundle) generated
- [ ] App size within limits
- [ ] Permissions justified in the Data Safety section
- [ ] `minSdkVersion` set appropriately (26)

### Data Safety Section
- [ ] Declare: No data collected
- [ ] Declare: No data shared with third parties
- [ ] Declare: Data is not processed (no analytics, no tracking)
- [ ] Declare: Camera permission used for QR scanning (WhatsApp Web)
- [ ] Declare: Microphone permission used for voice messages (WhatsApp Web)
- [ ] Link to privacy policy

### Testing
- [ ] App installs and launches on target devices
- [ ] Core flow works: onboarding → QR scan → messaging
- [ ] App handles no-network gracefully
- [ ] App handles process death and recreation
- [ ] Back navigation works correctly
- [ ] Theme switching works
- [ ] Settings persist across restarts
- [ ] No crashes in normal usage flow

## Post-Submission

- [ ] Monitor review status
- [ ] Respond to any policy clarification requests
- [ ] Monitor crash reports via Play Console
- [ ] Monitor user reviews
- [ ] Plan update cycle

## Important Disclaimers

- Google Play approval is **not guaranteed** for apps that wrap third-party web services.
- WhatsApp may update their terms or technical implementation at any time.
- The developer is responsible for ongoing compliance with all applicable policies.
- This checklist is a guide, not legal advice. Consult legal counsel before commercial distribution.
