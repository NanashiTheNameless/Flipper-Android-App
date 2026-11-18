# Nightly releases and signing

Flipper-Nanashi publishes nightly APK builds through GitHub releases, for Obtainium. Nothing is published to Google Play or F-Droid.

## How it works

`.github/workflows/nightly.yml` runs on every push to `dev` (and on manual dispatch):

1. Checks out the repository with its submodules and validates the Gradle wrapper.
2. Runs the unit tests (`testDebugUnitTest` for Android library modules, `testAndroidHostTest` for multiplatform modules).
3. Builds `:instances:android:app:assembleRelease` with `versionName` `<x.y.z>-<sha7>` and the time-based `versionCode`.
4. Signs with the release key from repository secrets. The decoded keystore lives in the runner's temp directory and is deleted right after the build.
5. Refuses to publish unless `apksigner` verifies APK Signature Scheme v3 and its V3 signer certificate matches the SHA-256 pinned in the `RELEASE_CERT_SHA256` repository variable. Every official build uses that one key.
6. Force-moves the `Nightly-Rolling` tag and updates the single `Nightly-Rolling` GitHub release (a normal release marked latest, not a pre-release) with:
   - `Flipper-Nanashi-nightly-<yyyy.mm.dd>-<sha7>.apk` (the current build and five previous dated APKs are kept)
   - `Flipper-Nanashi-nightly.apk` (always the newest; stable download URL)
   - `Flipper-Nanashi-obtainium.json` (Obtainium import configuration)
   - release notes with an APK/hash table

`.github/workflows/android.yml` runs the tests and a debug build on pull requests.

Install and update with Obtainium: use the button in the README, or add `https://github.com/NanashiTheNameless/Flipper-Android-App` (no pre-release setting needed).

## The release key

One key for every official build, forever. Android refuses an update signed by a different key, so losing or replacing it means uninstalling (and losing app data) on every phone. Keep at least two offline backups of the keystore and its password. Never commit it.

### 1. Generate it (once)

```bash
mkdir -p ~/.android-keys && chmod 700 ~/.android-keys
keytool -genkeypair -v -storetype PKCS12 \
  -keystore ~/.android-keys/flipper-nanashi-release.jks \
  -alias flipper-nanashi -keyalg RSA -keysize 4096 -validity 36500 \
  -dname "CN=NamelessNanashi"
```

With PKCS12 the key password is the keystore password.

### 2. Read the certificate fingerprint

```bash
keytool -list -v -keystore ~/.android-keys/flipper-nanashi-release.jks \
  -alias flipper-nanashi | grep 'SHA256:'
```

The fingerprint is public. Colons and case do not matter to the workflow.

### 3. Store it in GitHub

Run these yourself in a terminal. `gh secret set` without `--body` prompts for the value without echoing it.

```bash
REPO=NanashiTheNameless/Flipper-Android-App
base64 -w 0 ~/.android-keys/flipper-nanashi-release.jks | gh secret set RELEASE_KEYSTORE -R "$REPO"
gh secret set RELEASE_KEYSTORE_PASSWORD -R "$REPO"
gh secret set RELEASE_KEY_PASSWORD -R "$REPO"
gh secret set RELEASE_KEY_ALIAS -R "$REPO" --body flipper-nanashi
gh variable set RELEASE_CERT_SHA256 -R "$REPO" --body '<SHA256 fingerprint from step 2>'
```

If `RELEASE_CERT_SHA256` is missing, the workflow fails and prints the fingerprint it found, so you can check it and set the variable.

### 4. Local signed builds (optional)

Create `../flipper-nanashi-keystore.properties` next to the repository (outside it):

```properties
storeFile=/home/<you>/.android-keys/flipper-nanashi-release.jks
storePassword=...
keyAlias=flipper-nanashi
keyPassword=...
```

`./build.sh release` then signs with it and prints the certificate. Environment variables `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` override the file. With neither, the release build is unsigned. Debug builds always use the local debug key and install separately as `dev.namelessnanashi.flipperzero.debug`.

`./build.sh installRelease` runs the unit tests, builds the release APK, verifies and prints its signing certificate, then installs it on the connected phone with `adb install -r`. It requires a configured signing key and an authorized adb device.

## Version numbers

- `versionCode` is seconds since 2026-01-01T00:00:00Z, read at build time (`BuildVersionCode` in `build-logic`). It always increases and fits in an `Int` until 2094-01-19. Do not move that epoch backwards.
- `versionName` is `<x.y.z>-localbuild` locally and `<x.y.z>-<sha7>` in CI (`-PversionSuffix=`). Debug adds `-debug`. `<x.y.z>` is `ApkConfig.BASE_VERSION`.
