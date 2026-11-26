# Flipper-Nanashi

> [!WARNING]
> Unofficial, modified, personal fork of the [Flipper Zero Android app](https://github.com/flipperdevices/Flipper-Android-App), maintained by NamelessNanashi for their own use. Not affiliated with or endorsed by Flipper Devices. Unstable. Not meant for adoption by anyone else, and no support is offered.

## Differences from upstream

- **Momentum firmware:** updates come from [Momentum's update server](https://up.momentum-fw.dev/firmware/directory.json), and RPC uses [Momentum's protobuf fork](https://github.com/Next-Flip/flipperzero-protobuf). Hold **No updates** to reinstall the available firmware.
- **OLED appearance:** a black-only, mostly monochrome interface with black headers, outlined controls, 0xProto throughout, and Momentum launcher/splash branding.
- **Connections and transfers:** faster Bluetooth transfers and file reads, Bluetooth/USB connection instructions, improved USB handshake/reconnect handling, and a warning when another Flipper app shares the connection. Active syncs, installs and transfers use a foreground service to keep working in the background; idle connections do not keep that service running.
- **Synchronization:** a dismissible progress popup with working/waiting status, reopen controls in Archive and Device, a Stop button, and a five-second cooldown after syncing.
- **Favorites:** add/remove installed and built-in apps through the app list or Archive. Reorder favorites in the ordering editor, including uncached Momentum paths, remote `.txt` files, scripts and directories; preserve their full paths and order during sync.
- **App management:** **Install All**, **Update All**, a **Not installed** filter, and **Check & Repair Apps**. Queued apps download concurrently with separate download/install progress; uploads stay sequential and are verified before installation. Installed-app lookup and update-state handling are also improved.
- **Screen controls:** Momentum screen-streaming colours and text input from the phone keyboard.
- **Tools → App sources & backups · Beta:** import apps from HTTPS links or local FAP/ZIP files, build source apps on ARM64 phones through Termux, and select the SDK matching the connected Momentum release or development revision automatically. Share `flipper-nanashi://app` links, retain installation provenance and check external-source apps for updates. [Setup, supported workflows and limits](docs/app-sources.md).
- **Portable backups · Beta:** import/export keys, notes, settings, favorites, saved devices, widget presets, app metadata and build recipes. Include binaries only when a source recipe cannot rebuild them; this backs up managed data, not the entire Flipper SD card.
- **Automatic cleanup:** remove completed source/build downloads and intermediates, release completed or abandoned catalog downloads, and retain installed build tools, metadata and files needed for backups.
- **Privacy:** no analytics, crash reporting or push notifications; Countly, Sentry and Firebase are removed. No Google Play Services or Wear OS companion app. Catalogs, remotes and key sharing still use Flipper Devices' servers.
- **Distribution:** a separate `dev.namelessnanashi.flipperzero` application ID and signed GitHub rolling nightlies with an Obtainium configuration; no Play Store updater or Google Play/F-Droid releases.

## Install

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%257B%2522id%2522%253A%2522dev.namelessnanashi.flipperzero%2522%252C%2522url%2522%253A%2522https%253A%252F%252Fgithub.com%252FNanashiTheNameless%252FFlipper-Android-App%2522%252C%2522author%2522%253A%2522NanashiTheNameless%2522%252C%2522name%2522%253A%2522Flipper-Nanashi%2522%252C%2522additionalSettings%2522%253A%2522%257B%255C%2522includePrereleases%255C%2522%253Afalse%252C%255C%2522filterReleaseTitlesByRegEx%255C%2522%253A%255C%2522%255ENightly-Rolling%255C%2522%252C%255C%2522apkFilterRegEx%255C%2522%253A%255C%2522%255EFlipper-Nanashi-nightly%255C%255C%255C%255C.apk%2524%255C%2522%252C%255C%2522useLatestAssetDateAsReleaseDate%255C%2522%253Atrue%252C%255C%2522releaseDateAsVersion%255C%2522%253Atrue%252C%255C%2522versionDetection%255C%2522%253Afalse%257D%2522%257D"><img src="docs/assets/badge_obtainium.png" alt="Add to Obtainium" height="128"></a>

Nightly APKs only: download `Flipper-Nanashi-nightly.apk` from the [Nightly-Rolling release](https://github.com/NanashiTheNameless/Flipper-Android-App/releases/tag/Nightly-Rolling), or use the Obtainium button above, which is preconfigured for rolling nightly updates. [Import configuration](docs/Flipper-Nanashi-obtainium.json). Releases are published on GitHub only, never on Google Play or F-Droid.

> [!NOTE]
> Nightly artifacts become available after the GitHub Actions release workflow completes successfully.

## Thanks

- [0xProto](https://github.com/0xType/0xProto) by 0xType: a beautifully clear, distinctive typeface that deserves to be everyone's default. It's the only font in this app, used for every piece of text in place of Roboto, Roboto Mono and the Flipper pixel fonts. Thank you, 0xType! Licensed under the [SIL Open Font License 1.1](docs/fork/licenses/0xProto-OFL.txt).

## Support this fork

- [GitHub Sponsors](https://github.com/sponsors/NanashiTheNameless)
- [Buy Me a Coffee](https://buymeacoffee.com/NamelessNanashi)
- [Ko-fi](https://ko-fi.com/NanashiTheNameless)
- [Liberapay](https://liberapay.com/NamelessNanashi)
- [Throne](https://throne.com/NamelessNanashi)

## Build

Run `./build.sh test` for all Android unit tests without assembling an APK.
It discovers modules with test sources, including new untracked tests, and
avoids preparing empty test variants. It accepts Gradle flags, for example
`./build.sh test --continue`. Repeated runs reuse the Gradle daemon and cached
compilation/test outputs. For a single
module, pass its task directly, such as
`./build.sh :components:faphub:source:impl:testAndroidHostTest`.

```bash
git submodule update --init --recursive
./gradlew :instances:android:app:installDebug
```

The debug build installs as `dev.namelessnanashi.flipperzero.debug` ("Flipper-Nanashi (DEBUG)"), next to any other Flipper app.

## Module arch

```
├── instances
│   ├── app
├── components
│   ├── core
│   ├── bridge
│   ├── feature1
│   ├── feature2
```

- `app` - Main application module with UI
- `components/core` - Core library with deps and utils
- `components/bridge` - Communication between android and Flipper
- `components/*` - Features modules, which connect to root application
