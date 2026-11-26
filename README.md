# Flipper-Nanashi

> [!WARNING]
> Unofficial, modified, personal fork of the [Flipper Zero Android app](https://github.com/flipperdevices/Flipper-Android-App), maintained by NamelessNanashi for their own use. Not affiliated with or endorsed by Flipper Devices. Unstable. Not meant for adoption by anyone else, and no support is offered.

## Differences from upstream

- Targets [Momentum firmware](https://github.com/Next-Flip/Momentum-Firmware): firmware updates come from Momentum's update server, and the RPC protocol comes from Momentum's protobuf fork.
- No analytics, crash reporting or push notifications (Countly, Sentry and Firebase removed).
- No Google Play Services, Play Store updater or Wear OS companion app.
- Faster Bluetooth transfers.
- **Tools → App sources & backups · Beta:** link/file app imports, on-phone source builds through Termux, update checks and portable backups. [Setup and supported workflows](docs/app-sources.md).

Fork plans and status: [docs/fork/ROADMAP.md](docs/fork/ROADMAP.md).

## Install

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%257B%2522id%2522%253A%2522dev.namelessnanashi.flipperzero%2522%252C%2522url%2522%253A%2522https%253A%252F%252Fgithub.com%252FNanashiTheNameless%252FFlipper-Android-App%2522%252C%2522author%2522%253A%2522NanashiTheNameless%2522%252C%2522name%2522%253A%2522Flipper-Nanashi%2522%252C%2522additionalSettings%2522%253A%2522%257B%255C%2522includePrereleases%255C%2522%253Afalse%252C%255C%2522filterReleaseTitlesByRegEx%255C%2522%253A%255C%2522%255ENightly-Rolling%255C%2522%252C%255C%2522apkFilterRegEx%255C%2522%253A%255C%2522%255EFlipper-Nanashi-nightly%255C%255C%255C%255C.apk%2524%255C%2522%252C%255C%2522useLatestAssetDateAsReleaseDate%255C%2522%253Atrue%252C%255C%2522releaseDateAsVersion%255C%2522%253Atrue%252C%255C%2522versionDetection%255C%2522%253Afalse%257D%2522%257D"><img src="docs/assets/badge_obtainium.png" alt="Add to Obtainium" height="128"></a>

Nightly APKs only: download `Flipper-Nanashi-nightly.apk` from the [Nightly-Rolling release](https://github.com/NanashiTheNameless/Flipper-Android-App/releases/tag/Nightly-Rolling), or use the Obtainium button above, which is preconfigured for rolling nightly updates. [Import configuration](docs/Flipper-Nanashi-obtainium.json). Releases are published on GitHub only, never on Google Play or F-Droid.

> [!NOTE]
> The release workflow is not set up yet, so there is nothing to install from Obtainium until the first nightly is published.

## Thanks

- [0xProto](https://github.com/0xType/0xProto) by 0xType: a beautifully clear, distinctive typeface that deserves to be everyone's default. It's the only font in this app, used for every piece of text in place of Roboto, Roboto Mono and the Flipper pixel fonts. Thank you, 0xType! Licensed under the [SIL Open Font License 1.1](docs/fork/licenses/0xProto-OFL.txt).

## Support this fork

- [GitHub Sponsors](https://github.com/sponsors/NanashiTheNameless)
- [Buy Me a Coffee](https://buymeacoffee.com/NamelessNanashi)
- [Ko-fi](https://ko-fi.com/NanashiTheNameless)
- [Liberapay](https://liberapay.com/NamelessNanashi)
- [Throne](https://throne.com/NamelessNanashi)

## Build

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
