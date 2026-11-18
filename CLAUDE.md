# CLAUDE.md

Notes for AI agents working in this repository.

## What this repo is

An **unofficial, personal fork** of the Flipper Zero Android app (upstream:
`flipperdevices/Flipper-Android-App`), maintained by NamelessNanashi for their
own use. Fork repository: https://github.com/NanashiTheNameless/Flipper-Android-App
(`origin`; local `dev` tracks `origin/dev`). Upstream is the `upstream`
remote: fetch from it to pull in fixes, never push to it. It is not affiliated with or endorsed by Flipper Devices. It is not
meant for adoption by anyone else.

The fork's goals, decisions and per-item status live in
[`docs/fork/ROADMAP.md`](docs/fork/ROADMAP.md). Read it before starting work and
update it when an item's status or a decision changes.

## Rules

- Never add `Co-Authored-By` or other AI attribution to commits or PRs.
- Commit or push only when asked. Work directly on `dev`; do not create branches.
- Do not commit as items finish: everything goes in one commit at the end, after the
  user has cleaned up the docs.
- Do not reintroduce analytics, crash reporting, telemetry, Google Play or
  Firebase dependencies.
- Release builds are distributed through GitHub releases for Obtainium, never Google Play.

## Layout

- `instances/android/app` - the Android application module (app id `dev.namelessnanashi.flipperzero`, debug `.debug`).
- `components/**` - feature modules, usually split `api` / `impl` (plus `noop` for optional features).
- `components/bridge/connection/transport/ble` - BLE transport (Nordic Kotlin-BLE-Library 1.3.1).
- `components/bridge/connection/transport/usb` - USB serial transport: `desktopMain` uses jSerialComm, `androidMain`
  talks CDC-ACM through the Android USB host API and auto-switches to USB when a Flipper is plugged in.
- `components/core/ui/theme` - Compose theme: V1 `FlipperPallet.kt` + `FlipperPalletProvider.kt` and generated V2
  `pallet/generated/{Dark,Light}Pallet.kt`. Accents are monochrome (fork). New per-theme V1 colours must be
  constructor parameters AND listed in `AnimatedFlipperPalletProvider.toAnimatePallet`, or the dark value is lost.
- `components/updater/downloader` - firmware directory URL (`DownloaderApiImpl.kt`, Momentum's `up.momentum-fw.dev`).
- `components/faphub/**` - app catalog (`catalog.flipperzero.one`).
- `build-logic/plugins/convention` - convention plugins; `ApkConfig.kt` holds app id, SDK levels and feature flags.
- Packages are `dev.namelessnanashi.flipperzero.*`, except `com.flipperdevices.protobuf.*`
  (generated from the protobuf submodule). After merging upstream, run
  `python3 scripts/rename_package.py` to rename newly merged code (it also renames
  package-named files such as Room's `schemas/<package>.AppDatabase`).
- `components/nfc/tools/jni` - builds `libmfkey32.so` with CMake (multiplatform modules can't); needs the NDK.
- `components/bridge/connection/feature/rpc/impl/.../storage/FRequestStorage.kt` - request queue with the storage
  write gate: while a multi-chunk write is open, no other storage request may be sent (the firmware would
  truncate the file). Uploads go to `<path>.fztmp` and are renamed into place on success.
- `gradle/libs.versions.toml` - version catalog.

## Build

```bash
./setup.sh                                # JDK 25, SDK, NDK 29.0.14206865, CMake 4.1.2, git submodules
./gradlew :instances:android:app:assembleDebug
./gradlew :components:bridge:connection:transport:ble:impl:testAndroidHostTest
```

Multiplatform modules (`flipper.multiplatform`) put Android unit tests in
`src/androidHostTest` and run them with `testAndroidHostTest`. Tests in
`src/androidUnitTest` are silently skipped (`NO-SOURCE`). Don't pipe Gradle into
`tail` when checking results: the pipe hides Gradle's exit code.

Gradle builds are slow (hundreds of modules); run long builds in the background
and scope tests to the modules you touched.
