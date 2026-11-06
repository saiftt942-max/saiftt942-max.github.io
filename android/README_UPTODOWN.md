# Niyvexa — Android demo build

## Honest release state
This is a native Android, offline-first demonstration of intent-based matching. It does **not** connect real users. All profiles are fictional, and likes, matches, blocks, reports and demo messages are stored only on the device. Do not list it as a live dating network until a secure backend, age assurance and moderation are implemented and tested.

## Build
The project is in `android/`. GitHub Actions builds a signed-by-debug-key **test APK** on every relevant push:
- GitHub repository → Actions → “Build Niyvexa Android APK”
- Download artifact `Niyvexa-0.1.0-demo-DEBUG`
- This APK is for device testing only, not a recommended public release.

A publication-ready release requires a persistent private signing key. Add these repository Actions secrets under Settings → Secrets and variables → Actions:
- `NIYVEXA_KEYSTORE_BASE64`: Base64 of your private upload keystore file
- `NIYVEXA_KEYSTORE_PASSWORD`
- `NIYVEXA_KEY_ALIAS`
- `NIYVEXA_KEY_PASSWORD`

Keep the keystore and passwords private. Do not commit the keystore or passwords to the public repository. Then rerun the workflow; it will build and verify a signed release APK.

## Uptodown submission
Uptodown requires a developer account and an app file, then editorial review. Use distribution model **Demo**, Android, package `app.niyvexa.mobile`, version `0.1.0`; mark IAP, subscription and ads as **No** for this build. Suggested title, category, age note, short/full descriptions and legal-page links are in `listing/uptodown-metadata.md`.

Do not submit the debug artifact. A signed APK is necessary, but signature alone does not guarantee approval. Uptodown may reject software that does not meet its quality criteria. The developer must personally submit the APK through their account and confirm authorship and distribution rights.
