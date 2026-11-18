#!/usr/bin/env bash
# Sets up the JDK, SDK and submodules (see setup.sh), then runs Gradle.
#
#   ./build.sh                 unit tests + debug APK
#   ./build.sh install         unit tests + install the debug build on the connected phone
#   ./build.sh release         unit tests + release APK, then show its signing certificate
#   ./build.sh installRelease  unit tests + signed release APK + install on the connected phone
#   ./build.sh <tasks>         any Gradle tasks and flags, passed through
#
# The Gradle daemon stays running between builds, so repeat builds are much faster.

set -eu

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

FZ_SETUP_QUIET=1 . "$ROOT_DIR/setup.sh"

BUILD_TOOLS_DIR="$ANDROID_HOME/build-tools/37.0.0"
APP=":instances:android:app"
APK_DIR="instances/android/app/build/outputs/apk"
# Android library modules run unit tests with testDebugUnitTest, multiplatform modules with testAndroidHostTest
TEST_TASKS=(testDebugUnitTest testAndroidHostTest)

case "${1:-}" in
    "")
        exec ./gradlew "${TEST_TASKS[@]}" "$APP:assembleDebug"
        ;;
    install)
        shift
        if ! adb get-state >/dev/null 2>&1; then
            echo "error: no phone connected over adb (adb devices shows nothing ready)." >&2
            exit 1
        fi
        exec ./gradlew "${TEST_TASKS[@]}" "$APP:installDebug" "$@"
        ;;
    release|installRelease)
        action="$1"
        shift
        if [ "$action" = installRelease ] && ! adb get-state >/dev/null 2>&1; then
            echo "error: no phone connected over adb (adb devices shows nothing ready)." >&2
            exit 1
        fi
        ./gradlew "${TEST_TASKS[@]}" "$APP:assembleRelease" "$@"
        apk="$APK_DIR/release/app-release.apk"
        if [ ! -f "$apk" ]; then
            if [ "$action" = installRelease ]; then
                echo "error: release APK is unsigned; configure a signing key (see docs/releases.md)." >&2
                exit 1
            fi
            echo "Release APK is unsigned: no signing key configured (see docs/releases.md)."
            ls "$APK_DIR"/release/*.apk
            exit 0
        fi
        echo "APK: $apk"
        echo "APK SHA-256: $(sha256sum "$apk" | cut -d' ' -f1)"
        certificate_info=$("$BUILD_TOOLS_DIR/apksigner" -J-enable-native-access=ALL-UNNAMED verify --verbose --min-sdk-version 26 --print-certs "$apk")
        certificate_sha256=$(printf '%s\n' "$certificate_info" | bash "$ROOT_DIR/scripts/apk_certificate_sha256.sh")
        printf '%s\n' "$certificate_info" | grep 'certificate DN' || true
        printf 'Signing certificate SHA-256: %s\n' "$certificate_sha256"
        if [ "$action" = installRelease ]; then
            adb install -r "$apk"
        fi
        ;;
    *)
        exec ./gradlew "$@"
        ;;
esac
