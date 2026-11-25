# Fork roadmap

Unofficial personal fork for NamelessNanashi, to be published at
https://github.com/NanashiTheNameless/Flipper-Android-App. Status per item:
`todo` / `in progress` / `done` / `blocked (question)`.

Requested 2026-10-28.

| # | Item | Status |
|---|------|--------|
| 1 | Faster Bluetooth | mostly done (leftover ideas listed below) |
| 2 | USB-C to USB-C (USB host serial) connection on Android | written (uncommitted; builds, untested on device) |
| 3 | Momentum firmware instead of official firmware | done (uncommitted) |
| 4 | Install-All and Update-All buttons for apps | done (uncommitted; needs device test) |
| 5 | True black (OLED/AMOLED) dark theme | done (uncommitted; builds, needs a screenshot) |
| 6 | Remove the orange header | done (uncommitted; verified on device) |
| 7 | Change namespace to `dev.namelessnanashi.flipperzero` | done (uncommitted; builds and installs) |
| 8 | Build system modelled on FieldWatch-Nanashi | done (rehearsed locally 2026-11-18; first CI run on push) |
| 9 | Remove all analytics and tracking | done (uncommitted) |
| 10 | Drop Google Play, release through Obtainium | done (release key and GitHub secrets set 2026-11-18) |
| 11 | As monochrome as practical without losing function | written (uncommitted; compile pending, needs screenshots) |
| 12 | Make it clear this is an unofficial personal fork | done (uncommitted) |
| 13 | Check for broken/corrupted apps and repair them with one button | done (uncommitted; builds, untested on device) |
| 14 | App installs download the full `.fap` into the phone cache, verified, before uploading | done (uncommitted; builds, untested on device) |
| BUG | Storage writes corrupt files on the Flipper (truncated `.fap`s, empty `favorites.txt`) | fixed (uncommitted; tests pass, app builds; untested on device) |
| 15 | Warn when another Flipper app shares the connection | built (2026-11-18; untested on device) |
| 16 | Momentum screen colours in screen streaming, phone keyboard | built (2026-11-18; untested on device) |
| 17 | Faster file reads (was "file-manager download pipelining") | built (2026-11-18; tests pass) |
| 18 | Faster app installs | built (2026-11-18; untested on device) |
| 19 | Faster installed-apps lookup | built (2026-11-18; tests pass; untested on device) |
| 20 | Updated apps still showing "Update" | fixed (2026-11-18; tests pass; untested on device) |
| 21 | Remove the forum link; experimental features on by default | done (2026-11-18) |
| 22 | Dark launcher icon with the Momentum M | done (2026-11-18; check on the launcher) |
| 23 | "Not installed" filter in the app lists | built (2026-11-18; untested on device) |
| 24 | Key export named `Flipper-<name>-Keys.zip` | done (2026-11-18; tests pass) |
| 25 | Keep working in the background (especially sync) | built (2026-11-18; untested on device) |
| 26 | Fix "no SD card" on install after a reconnect | built (2026-11-18; untested on device) |
| 27 | Connection guide for Bluetooth and USB | built (2026-11-25; untested on device) |
| 28 | Less running in the background | built (2026-11-25; untested on device) |
| 29 | Every feature over USB, faster than Bluetooth | built (2026-11-25; untested with a Flipper on the cable) |
| 30 | Hold "No updates" to reinstall ("Update anyway") | built (2026-11-25; untested) |
| 31 | Unofficial-build notice on every clean launch (switch in Options) | written (2026-11-25; not built) |
| 32 | Cancel/Stop as real buttons that cancel immediately | written (2026-11-25; not built) |
| 33 | 0xProto font everywhere | written (2026-11-25; not built) |
| 34 | Download and install shown as separate steps on app buttons | written (2026-11-25; not built) |
| 35 | Sync screen throbber and working/waiting status | written (2026-11-25; not built) |
| 36 | Launch splash: new icon and the fork's credit | written (2026-11-25; not built) |
| 37 | Dismissible sync popup, automatic on sync start, Device-tab status/open button | built (2026-11-25; latest device verification pending) |
| 38 | Edit and reorder all Momentum favorite paths, including remote .txt files | built (2026-11-25; regression tests pass; final phone verification deferred) |
| 39 | Remove obsolete policy acceptance state | built (2026-11-25; migration test passes) |
| 40 | Reconnect after manual disconnect and USB replug | built (2026-11-25; reconnect regression tests pass; final phone verification deferred) |
| 41 | Parallel app downloads with live DL percentages | built (2026-11-25; eight prefetch downloads; sequential installs; regression test passes) |
| 42 | Check & Repair progress popup, Dismiss and Cancel | built (2026-11-25; latest device verification pending) |
| 43 | USB icon during firmware upload; black outlined action buttons | built (2026-11-25; latest device verification pending) |
| 44 | OLED dark theme as the only supported theme | built (2026-11-25; final debug APK builds; phone verification deferred) |

Remaining work (updated 2026-11-18):
1. On the device with the Flipper, storage-fix first: sync, one app install, one
   favorite change; confirm no truncated files and an intact `favorites.txt`.
2. Then, on the device: background sync (item 25), USB speed (large file download),
   Install All / Update All, "Update" state after updating (20), "Not installed" filter
   (23), faster installs and lookup (18, 19), Check & Repair, verified installs, mfkey32
   key recovery, other-app warning (15), keyboard and Momentum screen colours (16), key
   export name (24), launcher icon (22).
3. Screenshots of items 5 and 11 in the sole supported OLED dark theme; fix what looks wrong.

Settled 2026-11-18:
- Items 8/10: release key `~/.android-keys/flipper-nanashi-release.jks` (password in
  `../flipper-nanashi-keystore.properties`) is backed up offline. Secrets and
  `RELEASE_CERT_SHA256` (`5A:0E:09:DE:...:E8:D8`) are set; nightlies publish signed APKs.
- The "Card inputs" info log in `UpdateCardViewModel` stays.
- Work is committed and pushed to `dev` as it is finished (each push builds a nightly).


## Current continuation (2026-11-25)

- Sync opens its progress popup by default when work starts on a connected Flipper. It
  does not cover pairing, the fork notice, or firmware flashing. Dismiss keeps sync
  running; both Archive and the Device tab expose status and a button to reopen it.
  Shared Compose strings use indexed placeholders for file names and transfer speed.
- Favorites use the complete ordered path list, rather than only the key database.
  This includes `subghz/remote/BedRoom.txt`, `subghz/remote/WorkRoom.txt`, scripts,
  directories, internal files, and Momentum `/app:` shortcuts. Absolute targets are
  preserved. Favorites absent from the key cache remain visible in Archive and the
  editor; tapping one opens management instead of a key screen that cannot load it.
  The editor supports search, add/remove, move up/down, adding a path, Save and Cancel.
  Order-only changes synchronize both ways; file writes are read back and verified.
- Removed the old `tos_passed` field and acceptance APIs. Its protobuf tag/name remain
  reserved; migration removes the retired field while keeping setup and device data.
  The fork notice uses process state and the existing hide-notice option.
- Connect always issues a retry, including after manual Disconnect. Device status uses
  the saved selection for USB-only setups; opening pairing again skips a notice already
  shown in this process. The USB setup button clears the manual-disconnect state.
- USB open drops DTR before raising it, requires a CLI/RPC handshake, and never reports
  Connected if that handshake fails. Missing USB devices are removed on rescan.
  Automatic retries wait one second and use the current selection instead of an old
  USB path. Feature requests cancel initialization from a previous connection.
- Eight queued app bundles download concurrently into the phone cache. Per-bundle locks
  prevent duplicate writes. Live progress reaches queued DL labels, including DL 100%
  when waiting to install. Flipper installs remain sequential.
- Check & Repair shows the current app, checked count, stage, download percentage, and
  overall progress in a dismissible popup. Dismiss keeps work running; the installed-tab
  button reopens it. Cancel stops checking or queueing.
- Firmware upload uses a USB icon for USB connections. Download progress and action
  buttons use matching text/borders on black instead of filled blocks.
- Only the OLED dark theme is supported, including previews, splash/system bars, and
  error illustrations. Android API-specific style overrides also use the dark parent
  and light system-bar icons. The light palette, conditional artwork selection, theme
  selector, light preview, and stored selection field were removed (old protobuf
  tag/name reserved).
- Validation: 120 focused regression tests passed and the Android debug APK built.
  Final phone checks are deferred at the user's request; no firmware was flashed.
  Earlier phone checks verified formatted sync status, Dismiss, and favorites draft
  reorder/Cancel before the latest full-path favorites and dark-theme cleanup.

## Where work stopped (2026-10-28, paused by the user)

Item 6 (orange header), in progress:
- Done and verified on the phone: headers are black (dark) / off-white (light).
  V2 palettes: `navBar.body.accentBrand` = `navBar.body.main`. V1 palette gained
  `appBar` / `onAppBar` constructor fields (were a fixed body property, so the dark
  value never applied; also added to `toAnimatePallet`). `ComposableDeviceBar`,
  `ComposableDeviceInfoScreen`, `ComposableFapHubSwitch`, `EditorAppBar` and the
  settings status bar use `appBar`. `OrangeAppBar` title/back arrow, `SharedTopBar`,
  file manager / remote-control header icons use the normal title/icon colours.
- **Not built yet, last change before the pause:** the status bar icons were
  invisible (dark icons on the black header), which the user reported as "the app
  must not hide the notification bar". Two causes, both changed:
  1. `SingleActivity` now sets `isAppearanceLightStatusBars/NavigationBars` from
     `MaterialTheme.colors.isLight` (app theme) in a `SideEffect`; `enableEdgeToEdge()`
     used the system theme.
  2. `ScreenDecomposeComponent` forced dark icons (`isStatusBarIconLight = false`) on
     every screen resume. `StatusBarIconStyleProvider.isStatusBarIconLight` now returns
     `Boolean?`; the base returns `null` and `AndroidWindowDecorator` skips null.
  Next: build, install, screenshot to confirm the clock/battery icons show in dark
  and light themes. The XML themes still set `windowLightStatusBar=true` (only
  matters before Compose starts).
- Still orange in headers (item 11): the selected-tab pill in tab switchers
  (`ComposableTabswitch` / FapHub switch `accent`).

Update card (resolved 2026-10-29): with the Flipper properly connected the card shows
"No updates" on Dev `d3f89dfe`, matching the logged inputs. The info log line
"Card inputs: ..." in `UpdateCardViewModel` is still there; keep or drop it before
the final commit. Earlier notes:
- After the update-offer fix the user confirmed no UPDATE on Dev `d3f89dfe`, but a
  later install showed UPDATE again. An info log was added to `UpdateCardViewModel`
  ("Card inputs: ..."): inputs were correct every time (channel DEV, Flipper DEV
  `d3f89dfe`, always-update false), and Momentum's latest Dev is still `d3f89dfe`.
  With those inputs `UpdateCardHelper` returns no update, so look at what the card UI
  renders for `NoUpdate` and at screen-to-state timing before changing logic. Remove
  or keep the log line deliberately.

Also untested on the phone: Install All (item 4).

Favorites lost (reported 2026-10-29): superseded. The earlier guess (official app and
debug build syncing at once) was wrong; see "Storage write corruption" below.

## 15, 16, 17 (requested 2026-10-29)

15. Another app connected: two apps share one RPC session, so each receives the other's
    answers. `FRequestStorage` remembers the last 4096 sent command ids (`wasSent`);
    `FProtobufMessageCollector` feeds answers to unknown non-zero ids into
    `ForeignClientDetector` (3 within 30 s, at most one warning per 5 min), exposed as
    `FRpcFeatureApi.otherClientDetected()`. `OtherFlipperAppWarning` (app module) shows a
    10 s in-app error. Heuristic: if both apps happen to use the same ids it can't tell.
16. Momentum extras in screen streaming:
    - Colours: `ScreenFrame.fg_color` / `bg_color` are Momentum's packed
      `ScreenFrameColor` (byte 0 mode: 0 default, 1 custom RGB, 2 rainbow; bytes 1..3 RGB).
      `MomentumScreenColor.resolve` decodes them; rainbow cycles the hue over time;
      default keeps the orange LCD look. Test: `MomentumScreenColorTest`.
    - Keyboard: new "Keyboard" option next to Screenshot/Lock opens a text box;
      `FScreenStreamingFeatureApi.sendAscii(text)` sends one `SendAsciiEventRequest` per
      character (<= 0xFF) and stops at the first error; failures show a dialog (needs
      Momentum and a screen with text input).
17. A single file download is already one streamed RPC answer (no per-chunk round trips),
    so there was nothing to pipeline. Instead fixed `ReaderRequestLooper.getNextBytePack`,
    which busy-polled `tryReceive()` and kept a CPU core at 100% during every file read;
    the queue is now closed when reading stops and `receiveCatching()` suspends.
    2026-11-18: upstream #1115 (pulled) made the same change; took upstream's version
    (`queue.receive()`, so an early close raises an error, not a `CancellationException`).
    Also fixed an upstream `FFlipperSource` bug: when the last pack was split across two
    reads, the bytes left in the buffer were dropped (EOF returned first). Test added.
18. Faster app installs (requested 2026-11-18). Upload is already streamed (each chunk
    waits only for the local send, not a Flipper ack), so its speed is the transport's
    (item 1). The app-side waits around it are removed instead:
    - `FapQueueRunner` prefetches the bundles of queued Install/Update requests (3 at a
      time) into the cache while the current app uploads; a failed prefetch is only logged.
    - `FapDownloadApiImpl` locks per bundle file, so a prefetch and the install never share
      a `.part` file; the body is written through one stream (upstream reopened and stat'ed
      the file per 8 KiB packet).
    - Install/Update executors fetch the icon in parallel with the upload.
19. Faster installed-apps lookup (requested 2026-11-18). The catalog side is already one
    batched request (~0.2 s, HTTP/2); the time goes on Flipper round trips. Manifests were
    already md5-cached and read 4 at a time. `FapExistChecker` lists each app's category
    folder to confirm the .fap exists, but held one lock across the listing, so the folders
    were listed one after another. It now keeps one pending listing per folder (parallel
    across folders, shared within one; a cancelled listing is retried by its waiters).
    `FapManifestsLoader` prefetches the folders of all cached manifests at once and runs
    the check inside the parallel manifest reads. Test: `FapExistCheckerTest`.
20. Updated apps still said "Update" (reported 2026-11-18). `FapManifestApiImpl.add()`
    patched the in-memory manifest list, but a manifest reload already running (app start,
    opening the catalog list, pull-to-refresh, reconnect) then emitted the `.fim` contents it
    had read before the update and overwrote the patch. Installs/updates/deletes are now kept
    as local changes and applied on top of every reload until a reload starts after them;
    they are dropped on disconnect. `UpdateActionExecutor` also stopped copying the old
    manifest's `sourceFileHash`. Ruled out: the SDK check (catalog returns the build for the
    Flipper's API, Momentum 87.1, and that is what gets recorded). Test:
    `FapManifestApiImplTest` (fails without the fix).
21. Settings: the Forum link (and its string/icon) is gone. Experimental functions are on by
    default: proto field 4 `enabled_experimental_functions` is reserved and replaced by
    `disabled_experimental_functions = 35`, so existing installs switch to enabled too.
22. Launcher icon (requested 2026-11-18): true-black background and a white Momentum "M"
    (vector traced from Next-Flip/v2.momentum-fw.dev `public/logos/logo.svg`), sized to
    ~43dp of the 108dp canvas so it stays inside the 66dp safe zone under every mask. The
    monochrome (themed icon) layer uses the same M. The unused `mipmap-*dpi` foreground
    PNGs were removed (minSdk 26 always uses the adaptive icon).
23. "Not installed" chip next to the sort chip in the catalog tab and category lists
    (requested 2026-11-18), saved as `faphub_only_not_installed = 36`. The list skips
    apps whose uid is in the installed manifest instead of rebuilding the pager, so
    toggling it or installing an app keeps the scroll position. Hidden items are still
    read so paging keeps loading; the empty state shows when every loaded app is hidden.
24. Settings > Export keys writes `Flipper-<flipper name>-Keys.zip` (was `keys.zip`). The
    name comes from the paired BLE device (works offline); over USB, where the stored name
    is just "USB", the Flipper is asked for `hardware.name` (3 s limit). Unsafe characters
    become `_`; with no name it is `Flipper-Keys.zip`. Test: `KeysArchiveNameTest`.
25. Background (requested 2026-11-18; narrowed by item 28). Upstream deleted its Android service with the old
    bridge (#1017) and never replaced it, so nothing kept the process alive once the app left
    the screen: Android froze or killed it, dropping the connection, sync and transfers.
    `ConnectionKeepAlive` (app module) runs `FlipperConnectionService`, a `connectedDevice`
    foreground service, while a Flipper is connecting/connected or a sync is in progress,
    plus 15 s after a drop so the automatic reconnect survives too. Ongoing notification:
    "Connected to / Syncing with <Flipper>" with sync progress, tap to open, Disconnect
    action (force disconnect, so it does not reconnect). Permissions:
    FOREGROUND_SERVICE_CONNECTED_DEVICE, POST_NOTIFICATIONS (asked once), CHANGE_NETWORK_STATE
    (Android 14 prerequisite for USB-only use). Android 12+ refuses to start it while the
    app is already in the background (e.g. woken by the widget); that is logged, and the
    next connect with the app open starts it.
    Test on the phone: start a sync, switch apps / lock the screen, confirm it finishes.
26. Installing any app said "no SD card" while the device page showed it (reported
    2026-11-18). `FapManifestsLoader` took the `FStorageInfoFeatureApi` once per catalog
    refresh (`getSync`), but feature APIs are per connection: after a reconnect (background
    keep-alive, BLE -> USB switch) it kept watching the old instance, whose storage info
    never reached `Ready(Loaded)`, so every app got `NO_SD_CARD`. It now follows the current
    instance (`get()` + `flatMapLatest`), and storage info that is still loading waits
    instead of throwing `NoSdCardException`. Untested on device.
27. Connection guide (requested 2026-11-25). The first-pair "Help" screen (Bluetooth only,
    still linking Google Play) is replaced by `ConnectionGuideDecomposeComponent` (api in
    `firstpair/api`, impl in `firstpair/impl`): Bluetooth, USB cable and "Still not working?"
    sections, numbered steps, links (flipp.dev, momentum-fw.dev, the fork's GitHub releases)
    and a Bluetooth-settings shortcut. Opened from the first-pair "Guide" button and from
    Settings > Connection Guide. The first-pair screen itself has a "Connect with USB cable"
    button above "Skip Connection" (same action). During first setup the guide has a "Use USB" button: if a Flipper is
    on the cable (`FUsbFlipperSwitcherApi.isFlipperAttached()`), setup finishes without BLE
    pairing (USB-only users no longer have to pair or press Skip); otherwise it says none was
    found. Check on device: wording of the Flipper menu paths on Momentum.
    2026-11-25: the first launch screen (was "Turn On Bluetooth on your Flipper" + Flipper
    Devices' ToS/privacy links) is now an "Unofficial fork" notice with an "I understand"
    button; the search screen below it explains Bluetooth and USB in two lines. The
    `pic_connecting` / `pic_flipper_instruction` drawables were removed.
28. Less in the background (requested 2026-11-25). Changes item 25: the foreground service no
    longer runs for the whole connection, only while work is in flight: a sync, queued/running
    FapHub installs, or any RPC request waiting for an answer (file transfers, firmware upload;
    `FLagsDetectorFeature.hasPendingRequests()`, from the lags detector's pending counter). It
    stops 5 s after the work ends (15 s after a link drop, so a reconnect mid-work survives).
    An idle connection lets Android cache/freeze the app as usual. The notification is
    `FOREGROUND_SERVICE_DEFERRED`, so work shorter than ~10 s shows none; titles are
    "Syncing with / Installing apps on / Transferring files with / Reconnecting to <name>".
    Limitation: Android 12+ can't start the service from the background, so work must start
    while the app is open (as before). Also: the BLE and USB `SpeedMeter`s (two per link) woke
    every second for the whole connection; they now tick only while bytes flow.
29. USB parity and speed (requested 2026-11-25, after the device screen showed firmware
    "1.0.0 fakeusb0"). Upstream's USB transport implemented `FTransportMetaInfoApi` with a
    fake (`FakeTransportMetaInfoApi`, now deleted): name "USB Flipper", firmware
    "1.0.0 fakeusb0", protocol 0.24, battery 0 % (the 0 % also blocked firmware updates,
    which check the battery). USB devices no longer implement it; instead:
    - `FVersionFeatureFactoryImpl` falls back to `RpcApiVersionMetaInfoApi`
      (`system_protobuf_version_request`, 5 tries), so features gated on the API version use
      the real one.
    - `FGattInfoFeatureFactoryImpl` falls back to `RpcGattMetaInfoApi`: `devinfo` read once
      (name, hardware ver, software revision rebuilt as "<commit> <version> <branch num>
      <date>" for `FirmwareVersionBuilderApi`), `pwrinfo` polled every 30 s while observed
      (charge level, charging).
    - Device screen name falls back to the Flipper's reported name when setup was USB-only;
      the device picture colour comes from `hardware.color` for USB devices.
    - Speed: `FAndroidUSBSerialDeviceApi.sendBytes` queues; one writer joins queued RPC
      messages into transfers of up to 16 KiB (was one USB transfer per <= 512-byte message).
      Idle read timeout 100 -> 500 ms (returns immediately on data).
    - Footer on the first setup screen no longer claims Flipper Devices' ToS/privacy policy;
      it states the fork is unofficial, collects no data, and which Flipper servers it uses.
    - Reads: measured ~1 KB/s on the phone (motorola edge 2024). Answers arrived in bursts,
      17 s for a 166 byte file: on that phone a USB IN transfer completed only when its
      buffer (16 KiB) was full, not on the short packet that ends an answer. The read loop
      (`AndroidCdcAcmPort.readLoop`) now queues 32 one-packet (64 byte) `UsbRequest`s and
      passes data on after a short packet, 1 ms without a packet, or 16 KiB. Confirmed over
      USB: Momentum reports `firmware.branch.name=dev`, `firmware.version=mntm-dev`.
    - Stall after the app-catalog listing (2026-11-25, log): answers arrived but were never
      parsed and USB reads stopped. The RPC send/receive/parse loops block threads but ran on
      `FlipperDispatchers.workStealingDispatcher` (one thread per core) together with app work
      (loading ~600 manifests); with all threads busy nothing drained the received bytes. They
      now run on `FlipperDispatchers.blockingDispatcher` (Dispatchers.IO). Untested.
    - Real cause of the sync hang ("stuck on Garage.sub"), from a jdb thread dump: all 8
      `workStealingDispatcher` threads sat in `FFlipperSource.read` -> `runBlocking`
      (4 sync key reads in `FlipperKeyStorage`, 4 manifest reads in `FapManifestsLoader`),
      waiting for `ReaderRequestLooper`, whose coroutine also needed that pool: deadlock.
      The looper (and `WriteRequestLooper`) now run on `blockingDispatcher`, and the blocking
      readers/upload/download run there too. Also affects Bluetooth.
    - App updates stuck at 0 % over USB (log: `Receive package without id ... command_id=709,
      ERROR_STORAGE_EXIST`): `FRpcFeatureApiImpl.requestOnce` / `request(flow)` registered the
      answer listener in a dispatched `async` and sent the request right away; over USB the
      answer (~3 ms) could arrive first and was dropped, so the caller waited forever. The
      async now starts `UNDISPATCHED`, registering the listener before the send.
    - Disconnect / unplug left the app showing "Connected": the USB transport reported
      Connecting and Connected but never Disconnected (the orchestrator's state only changes
      on transport updates). `AndroidUSBDeviceConnectionApiImpl` now reports Disconnected once
      when the connection scope ends (Disconnect, device switch) or the read loop ends (cable
      pulled, `FAndroidUSBSerialDeviceApi.onLinkLost`).
    - Bottom bar "Connected" tab showed the Bluetooth glyph over USB: new `ic_connected_usb`
      (+ filled) with a USB trident, picked when the current device is the USB model.
    - Not a transport problem: `FapManifestsLoader` lists `/ext/apps_manifests` with
      `include_md5`, so the Flipper hashes ~600 files (17-42 s) and every other request waits
      behind it (the Flipper answers one at a time). That made sync look like ~1 KB/s.
    Still noop over USB: RPC restart after a hang (lags detector), so a hung session needs a
    replug. To check with the Flipper on the cable (use wireless adb): device screen values,
    battery, Momentum update offer, file transfer speed vs Bluetooth, app installs.

30. "Update anyway" (requested 2026-11-25). `UpdateCardState.NoUpdate` carries the channel's
    latest build (`reinstall`); holding the NO UPDATES button (haptic) asks "Update anyway?"
    and then runs the normal update request flow with that build.

31. Start-up notice (requested 2026-11-25). The first-launch "Unofficial fork" screen
    (`ComposableTOS`) also opens on top of the main screen once per process start
    (`RootScreenConfig.ForkNotice`, `FirstPairApi.consumeForkNoticeOnStart`), so a cold
    launch shows it and returning from the background, recents or a restored process does
    not. First setup counts as shown. Options > "Unofficial build notice" switches it off
    (`hide_fork_notice_on_start = 37`).
    "I understand" sits at the end of the scrolling content, so the notice has to be scrolled
    through before it can be dismissed.

32. Cancel buttons (requested 2026-11-25). New `ComposableCancelButton` (core/ui/ktx: outlined
    full-width pill) replaces the text links on the file-manager upload/download/editor
    progress screen (`FileTransferFullScreenComposable`), archive sync progress, the firmware
    updater screen and the Installed tab's "Cancel updating". Dialog buttons were already
    buttons. Immediate: upload/download view models cancel the job and update the screen at
    once (no `cancelAndJoin`; cancelled jobs can no longer report Uploaded/Error afterwards);
    the FapHub queue clears the current task at once and no longer reports a cancel as a
    failure; the updater screen finishes right after `cancel()` (a new update still waits
    for the old one); `SynchronizationApi.stop()` reports Finished at once. Editing screens'
    app-bar "Cancel" (key edit, infrared edit) stay app-bar text actions.
33. Font (requested 2026-11-25). Every `FlipperTypography` style and Material's default
    typography use 0xProto 2.502 (Regular/Bold/Italic, copied from the upstream release; SIL
    OFL, `docs/fork/licenses/0xProto-OFL.txt`). The Roboto, Roboto Mono and Flipper pixel
    fonts (`flipper`, `flipper_bold`, `flipper_action`) were removed. Not changed: system
    UI such as the notification and home-screen widgets, which use the system font.

34. Install steps (requested 2026-11-25). Install/update progress was one number (0-10 %
    downloading the bundle from the catalog, 10-99 % uploading to the Flipper). The shared
    boundary is `FAP_DOWNLOAD_PROGRESS_SHARE` (queue api); the in-progress app button shows
    "DOWNLOADING n%" then "INSTALLING n%" / "UPDATING n%" (compact: DL / INST / UPD), each
    step with its own 0-100 % and progress bar. A cached bundle skips straight to installing.

35. Sync screen activity (requested 2026-11-25). `ArchiveProgressScreen` shows a spinning
    throbber and a status dot under the step text: bright and "Working: n/s" while data moves
    (rx + tx > 0), dimmed with a hollow dot and "Waiting for the Flipper…" when nothing moved
    for a second (e.g. the Flipper hashing files). Replaces the "Speed:" line.

36. Splash (requested 2026-11-25). Was the Flipper dolphin, the Furippa lettering and the
    Flipper Devices wordmark (removed). Android 12+: the launcher foreground (Momentum "M") on
    the launcher's black icon background, branding image `ic_splashscreen_credit`. Older:
    `splash_screen.xml` layer-list with `pic_splashscreen_icon` (black rounded square + M) and
    the same credit. The credit ("Unofficial fork / by NamelessNanashi") is 0xProto glyph
    outlines generated with fontTools, colour `splash_credit` (dark/light).

Found while testing on the phone (2026-11-25):
- App install needed a second tap: the queue prefetch and the install downloaded the same
  bundle at once and the second deleted the first's `.part` ("incomplete: 0 of 33820
  bytes", install failed, button back to Install). `FapDownloadApiImpl` was not a singleton,
  so its per-bundle lock map (item 18) was per instance; it is now `@SingleIn` and the map
  is process-wide.
- Firmware update button: white label on the dark theme's light grey (and on the bright
  update green) was unreadable; the label is now black or white by the button's luminance.
- Verified over USB on the phone: first setup with "Connect with USB cable", device screen
  (name, battery 100 %, firmware "Dev d3f89dfe", build date, SD card), protocol 0.25 read
  over RPC, a full sync with no hang, app install (upload + md5 verify ~3.4 s for 33 KB).

## Storage write corruption (found 2026-10-29)

Evidence (diagnosed by another session, which also repaired the SD card): 56 of 624
FapHub `.fap`s were corrupt, many holding only the tail of the real file (e.g.
`flippar.fap` 616 B of 16488). `/ext/favorites.txt` went from 23 bytes to 0 at 04:29
while only the fork debug build was connected.

Firmware (Momentum `applications/services/rpc/rpc_storage.c`, confirmed): every
non-write storage handler starts with `rpc_system_storage_reset_state(send_error=true)`,
which closes an open write and sends `ERROR_CONTINUOUS_COMMAND_INTERRUPTED` to its id.
The next chunk with that id finds the session idle and reopens the file with
`FSOM_CREATE_ALWAYS`, so only later chunks survive. An empty `has_next=false` write
with a new id also opens with `CREATE_ALWAYS` and leaves a 0-byte file.
`storage_common_rename` removes an existing target before renaming.

App-side causes (all upstream; the fork's speed changes made the first one likelier):
1. Nothing kept a multi-chunk write exclusive: chunks share a priority queue
   (`FRequestStorage`) with every other request, so any storage command could be sent
   between chunks. Faster BLE (no 100 ms wait, write without response), 4 parallel sync
   reads, and the FapHub manifest loader running during installs all raise the odds.
2. After an error answer (e.g. `ERROR_CONTINUOUS_COMMAND_INTERRUPTED`) chunks already
   queued for that id were still sent, restarting the write.
3. `WriteRequestLooper` cancelled a write by sending an empty `has_next=false`
   `WriteRequest` to the target path: 0-byte file.
4. **Direct cause of the 04:29 wipe:** `FlipperKeyStorage.saveFile` did
   `sink(path).use { it.buffer().writeAll(source) }`. `writeAll` keeps a partial
   (< 8 KiB) segment buffered, and only the raw sink was closed, so every small file
   synced to the Flipper (favorites.txt, keys under 8 KiB) was written EMPTY. Upstream
   since "Migrate synchronization ... to new api" (`222e14fc7`).
5. `FlipperFavoritesRepository.applyDiff` rewrote favorites.txt on every sync with any
   favorites diff, even when nothing changed on the Flipper (04:29: only an
   `ADD, source=FLIPPER`), so cause 4 hit it every time.
6. `copyWithProgress` stopped quietly on cancellation; callers then closed the sink and
   treated a partial copy as complete.

Fix (uncommitted):
- `FRequestStorage`: write-session gate. Once a write chunk with `has_next=true` is
  sent, only that id's chunks and non-storage requests are sent until its final chunk;
  `abortWriteSession(id)` drops its queued chunks; a session idle for 60 s is released.
  `Main.isStorageRequest()` covers all 13 storage request types.
- `FRpcFeatureApiImpl.request(commandFlow)`: aborts the write session whenever the
  command ends (success, error answer, cancellation).
- `WriteRequestLooper`: cancel no longer sends the truncating empty write.
- `FFileUploadApiImpl.sink`: writes to `<path>.fztmp`, renames over the target only
  after the Flipper confirmed the write, deletes the temp file on failure.
  `FFlipperSink.close()` runs once (a second close would send another final chunk).
- `FlipperKeyStorage.saveFile`: closes the buffered sink.
- `FlipperFavoritesRepository.applyDiff`: no write when there is nothing to change on
  the Flipper; refuses to replace a non-empty list with an empty one unless the diff
  deletes favorites; reads the file back and fails if it differs.
- `copyWithProgress` throws on cancellation.
- FapHub installs already verify the uploaded `.fap` MD5 against the download (item 14).
Tests: `FRequestStorageTest` (gate), `FFlipperSinkTest` (small buffered file, close
once, commit/fail), `FlipperKeyStorageTest`, `FlipperFavoritesRepositoryTest`.
Until verified on the device: keep the debug build away from the Flipper (BT off or
force-stop `dev.namelessnanashi.flipperzero.debug`), and do not use Install All or
Check & Repair.

## 13, 14. Verified installs and Check & Repair (requested 2026-10-29)

Findings: installs already downloaded to a temp file in `cacheDir` and then uploaded
(`FapDownloadApiImpl` -> `FapActionUpload`), but nothing checked that the download
was complete (a dropped connection just ended the loop) or that the upload matched.
Either can leave a truncated `.fap` on the Flipper. The catalog has no checksum, and
`FapManifestItem.sourceFileHash` is the MD5 of the `.fim` manifest, not the `.fap`.

Done (uncommitted, builds; untested on device because the Flipper was not reachable):
- `FapDownloadApiImpl`: per-version cache file in `cacheDir/fap_bundles`, written via
  `.part`, HTTP status and Content-Length checked, complete files reused.
  `FlipperStorageProvider.getCacheFolder(name)` added.
- `FapActionUpload`: compares Flipper MD5 of `tmp.fap` with the local MD5, uploads
  once more on mismatch, then fails the install instead of installing a bad file.
- `FapRepairApi` / `FapRepairImpl` (installation/all): `check()` reports broken
  (missing or MD5 mismatch) and uncheckable apps; `repair()` queues an Update to the
  current catalog version for each broken app.
- Installed tab: "Check & Repair Apps" button (shown when apps are installed),
  spinner while checking, result dialog with app names and a Repair button,
  "Queued N repairs" afterwards (`ComposableRepairApps`, `RepairAppsViewModel`).

Original plan:
- 14: download each bundle into `cacheDir/fap_bundles/<versionUid>_<target>_<sdk>.fap`
  via a `.part` file; require HTTP success and, when the server sends it, size ==
  Content-Length; reuse a complete cached file. After uploading to the Flipper's tmp
  path, compare the Flipper MD5 (`md5Api()`) with the local MD5; retry once, then fail.
- 13: "Check & Repair" in the Installed tab. For each installed catalog app: `.fap`
  missing on the Flipper, or its MD5 differs from the cached bundle of the installed
  version (downloaded if not cached) -> queue a reinstall (update to the current
  catalog version). Offline apps (not in the catalog) are reported, not repaired.

## 1. Faster Bluetooth

Transport: `components/bridge/connection/transport/ble/impl`.

Findings:
- Serial writes used `BleWriteType.DEFAULT` (write with response): one ATT
  round trip per packet. Firmware RX characteristic
  (`19ed82ae-...-228e62fe0000`) supports `WRITE_WITHOUT_RESP`, and the app
  already has flow control (`FSerialOverflowThrottler`, overflow characteristic).
- No connection priority request, so Android used the default ~30-50 ms interval.
- `FSerialOverflowThrottler` waited up to 100 ms for more commands before every
  send, adding ~100 ms to every request.
- Connections always used `autoConnect = true` (slow background connect), even
  right after a scan found the device advertising.

Done (uncommitted; 18/18 module tests pass; needs on-device test):
- `FSerialUnsafeApiImpl`: `splitWrite(..., BleWriteType.NO_RESPONSE)`.
- `BleDeviceConnectionApiImpl`: `requestConnectionPriority(HIGH)` after MTU.
- `FSerialOverflowThrottler`: batch only already-queued commands (`tryReceive`), no 100 ms wait.
- `BLEConnectionDeviceHelper.fastConnection`: `autoConnect = false`; fallback by MAC keeps `true`.

Also moved the module's tests from `src/androidUnitTest` (skipped since the
multiplatform migration) to `src/androidHostTest` so they run again.

Measured on the user's phone (motorola edge 2024, Momentum, 2026-10-28):
- Link: 2M PHY (negotiated by the phone), MTU 414, connection interval 7.5 ms, Flipper RX buffer 1024 B.
- The official app running at the same time shares the BLE link and both apps
  send RPC on the same serial characteristic: sync stalls at 1% with 0 B/s.
  Force-stop the official app when testing the fork.
- Throughput ceiling Flipper -> phone: the serial TX characteristic uses
  INDICATE (one 243-byte packet per confirmation round trip), so roughly
  16-32 KB/s (130-260 kbit/s). 1 Mbit/s is impossible over BLE without a
  firmware change (TX as NOTIFY). USB is the path to >= 1 Mbit/s.
- Sync was latency-bound: one file read per round trip (~18 files/s).
  `DiffKeyExecutor` now keeps 4 Flipper reads in flight; writes stay ordered.
- FapHub manifest loader restarted on every storage-info update after connect;
  each restart abandoned an MD5 listing of `/ext/apps_manifests` that the
  Flipper still streamed in full (~18 s each). Now restarts only when
  connected/SD-ready changes.
- Remaining sync cost: the Flipper computes MD5 for every key file per folder
  (~40 s for a large `/ext/subghz`). Folders are skipped when their timestamp is
  unchanged since the last completed sync, and no sync had ever completed before.
- Second sync after a completed one: 53 s with 0 changes; 46 s of it is the
  Flipper hashing `/ext/subghz`. Momentum returns one storage-wide timestamp for
  every folder and it moves on any SD write, so the unchanged-folder skip never
  applies. Decided 2026-10-28: keep full MD5 hashing every sync (no size-based
  shortcut), because a same-size edit made on the Flipper would be missed.
- Further ideas: pipeline FapHub manifest file reads (one read per round trip,
  ~250 files at startup); pipeline file-manager downloads.

## 2. USB connection on Android

Written (uncommitted; app builds; untested with a Flipper on a cable):
- `transport/usb/impl`: the jSerialComm implementation moved from `jvmSharedMain` to
  `desktopMain` (it opens `/dev/tty*`, which Android apps can't). New `androidMain`:
  - `AndroidCdcAcmPort`: CDC-ACM over the Android USB host API, no extra library:
    claims the CDC control and data interfaces, SET_LINE_CODING 230400 8N1,
    SET_CONTROL_LINE_STATE DTR|RTS (the Flipper starts its CLI on DTR), bulk read/write.
  - `AndroidUSBDeviceConnectionApiImpl`: finds the device by `UsbDevice.deviceName`
    (`FUSBDeviceConnectionConfig.path`), asks for USB permission if needed, waits up to
    3 s for the CLI prompt `>: `, sends `start_rpc_session\r`, skips the echoed line,
    then hands over `FAndroidUSBSerialDeviceApi` (read loop + serialized writes).
  - `AndroidUsbFlipperSwitcher` (`FUsbFlipperSwitcherApi`): at app start and on
    `USB_DEVICE_ATTACHED` (delivered to `SingleActivity` through a manifest filter for
    VID 0x0483 / PID 0x5740) it adds/updates one USB device (`flipper-usb`,
    "Flipper (USB)") and makes it current; on `USB_DEVICE_DETACHED` it switches back to
    the previous device (or the first Bluetooth one) and removes the USB entry.
- Manifest: `android.hardware.usb.host` (not required), attach intent filter,
  `res/xml/usb_device_filter.xml`. App depends on `transport/usb/api` + `impl`.
- To check on the device: the CLI prompt and echo handling on Momentum, RPC speed,
  behaviour of BLE-only features over USB (battery/meta info is faked), and that the
  storage-write gate holds over USB too (it is transport independent).

## 3. Momentum firmware

Done (uncommitted):
- Protobuf submodule now `Next-Flip/flipperzero-protobuf` branch `dev`
  (`ea4f185`): official protocol plus `ScreenFrame.bg_color/fg_color` and
  `SendAsciiEventRequest` (Main field 100). Not used by the UI yet.
- Firmware directory: `https://up.momentum-fw.dev/firmware/directory.json`
  (same schema; `prNNN:*` channels are ignored through `coerceInputValues`).
- Momentum release builds report `mntm-NNN` as the branch in the BLE software
  revision; it now parses as RELEASE and compares numerically. Dev builds report
  `dev` and already parsed.
- Release Candidate hidden from the channel picker (Momentum has none).
- Channel picker offers **Mainline** (Momentum releases, `FirmwareChannel.RELEASE`)
  and **Dev**, plus Custom (update from file). The choice is saved in the settings
  DataStore (`selected_channel`) and wins over the Flipper's current channel;
  a stored `RELEASE_CANDIDATE` now maps to Mainline. Requested 2026-10-28.
- SubGhz region bundle still comes from `update.flipperzero.one` (region data only).
- Removed the update-card checks that offered an update when `/ext/Manifest` or
  `/int/.region_data` was missing or the phone's region differed from the Flipper's.
  Momentum has neither file, so the card always showed UPDATE even on the latest
  Dev build (reported 2026-10-28 on Dev `d3f89dfe`). The version comparison and the
  "always show update" debug switch remain.

Ideas: use Momentum screen colours in screen streaming; a phone keyboard via `SendAsciiEventRequest`.

## Fixed: mfkey32 native library not built

Upstream's "Android multiplatform" commit (`9ab0cc489`) removed the CMake
`externalNativeBuild` from `components/nfc/tools/impl`, so `libmfkey32.so` was not
in the APK. Also the package rename left the JNI symbol as `Java_com_flipperdevices_...`.
Fixed (uncommitted, verified in the APK, untested on device):
- New module `:components:nfc:tools:jni` (plain `com.android.library`) builds the
  existing `impl/src/main/cpp/CMakeLists.txt` with NDK `29.0.14206865` and CMake
  `4.1.2` for arm64-v8a, armeabi-v7a and x86_64; `nfc:tools:impl` depends on it.
- JNI function renamed to `Java_dev_namelessnanashi_flipperzero_..._tryRecoverKey`;
  `scripts/rename_package.py` now also rewrites `.c/.cpp/.h` and JNI names.
- `setup.sh`, `android.yml` and `nightly.yml` install `ndk;29.0.14206865` and `cmake;4.1.2`.

## 4. Install-All / Update-All

Already present: `FapInstallationAllApi.installAll()` (exposed only in the debug
settings category), and an Update-All button in the FapHub installed tab
(`ComposableUpdateAllButton*`). Work: surface Install-All in the normal FapHub
UI, and make Update-All always visible and working.

Scope (user, 2026-10-28):
- **Install-All** installs only catalog apps that are not currently installed;
  already installed apps are skipped (not reinstalled or updated).
- **Update-All** updates only installed apps that are out of date; up-to-date apps are skipped.
Done (uncommitted):
- Catalog tab has an **Install All** button above the app list, with a confirmation
  dialog, a spinner while queueing, and "Queued N apps" / error text afterwards
  (`ComposableInstallAll`, `InstallAllViewModel`).
- `FapInstallationAllApi.installAll()` (upstream already skipped installed apps) now
  also skips apps hidden by the user and apps whose build state is not installable
  (only `READY` / `READY_ON_RELEASE`), and returns how many it queued.
- Update-All needed no change: it only queues apps in `ReadyToUpdate` (outdated),
  shows the count, and hides when nothing is outdated.
- The expert-mode debug entry "Install all app" still exists and calls the same API.

## 5, 6, 11. Theme

Item 5, true black (dark theme only): backgrounds and the bottom bar `#000000` (with a
1 dp divider), cards `#111111`, dialogs / sheets / menus / pop-ups `#1A1A1A`, in both
the V1 and V2 dark palettes. `window.isNavigationBarContrastEnforced = false` so no
grey scrim sits behind the 3-button navigation bar.

Item 6, headers: done, see "Where work stopped" above.

Item 11, monochrome (written, needs screenshots):
- Inverted neutral accent so active and disabled stay distinct: dark theme accent
  `#E6E6E6` with black text/icons on it, light theme `#1A1A1A` with white on it.
  Secondary accent (was blue: links, Synchronize, Install All) `#BDBDBD` dark /
  `#3D3D3D` light. Lighter "in progress" shades and faint tints mapped to greys.
- V2 palettes: brand, fwInstall, blue, link, textField.selected, border.accentBrand,
  icon brand/blue, bluetooth pill; dark `onColor` for brand/fwInstall/blue -> black.
- V1 palette: accent, accentSecond, action-on-Flipper colours, share sheet action,
  FapHub open-app, `onFapHubInstallButton`, `onFlipperButton`, `tabSwitchActiveColor`
  (selected tab text: black on the light pill in dark, white on the dark pill in light),
  `updateProgressBackgroundBlue` are now per-theme constructor parameters.
- 45 drawables and 2 colour files: orange/blue -> `#8C8C8C`.
- Kept on purpose (they carry meaning): firmware channel colours, error/warning/
  success, battery, key-type colours, NFC editor key A/B/access-bit colours, the
  Flipper LCD orange (screen streaming, device mockup, `flipperScreenColor`).

## 7. Namespace

Decided 2026-10-28: **full package rename**, lowercase. Done with
`scripts/rename_package.py` (idempotent; re-run after upstream merges):
- Application id `dev.namelessnanashi.flipperzero` (debug adds `.debug`), set in `ApkConfig.APPLICATION_ID`.
- All Kotlin packages, source directories (301 moved), Gradle namespaces, our own
  `.proto` `java_package` options, manifests, ProGuard rules, geminio templates.
- Kept: `com.flipperdevices.protobuf.*` (generated from the Momentum protobuf
  submodule, which we do not edit), `github.com/flipperdevices` URLs, old
  `.github` workflows (replaced in item 8).
- A new app id means a fresh install: no data carries over from `com.flipperdevices.app`.

## 8. Build system (reference: NanashiTheNameless/FieldWatch-Nanashi)

Done (uncommitted):
- `.java-version` (25), `setup.sh` (JDK, SDK packages, git submodules, `local.properties`),
  `build.sh` (`""`, `install`, `release`, `installRelease`, passthrough). Tests run
  `testDebugUnitTest` + `testAndroidHostTest` across modules.
- `BuildVersionCode` ValueSource: `versionCode` = seconds since 2026-01-01 UTC, set
  per variant in `flipper.android-app`. `versionName` = `ApkConfig.BASE_VERSION`
  (`1.8.1`) + `-<sha7>` in CI or `-localbuild`; debug adds `-debug`. The desktop
  sample packages with the plain `BASE_VERSION`.
- Release signing in `flipper.android-app` from env (`KEYSTORE_FILE`,
  `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or
  `../flipper-nanashi-keystore.properties`; v2+v3 only; unsigned when absent.
- Workflows: upstream's removed (`internal*.yml`, `release.yml`, `pr.yml`,
  `call-*.yml`, `targets/`, `version.env`). New `android.yml` (PR: tests, debug APK)
  and `nightly.yml` (push to `dev`: tests, signed release, V3 cert check against
  `RELEASE_CERT_SHA256`, `Nightly-Rolling` release with dated + stable APK + Obtainium
  JSON, keeps 5 old APKs). Actions pinned to commit SHAs. Checkout includes submodules.
- `scripts/apk_certificate_sha256.sh`, `scripts/test_apk_certificate.sh` (passes).
- `docs/releases.md`.

Still needed before the first release: `RELEASE_*` secrets and `RELEASE_CERT_SHA256`
on the fork (see `docs/releases.md`). Not run yet: the full test suite across all
modules and an R8-minified release build, so the first CI run may surface failures.

Left for the docs cleanup: `.github/CODEOWNERS`, `renovate.json5`, `changelog/`,
`ISSUE_TEMPLATE/`, `pull_request_template.md` and `dependabot.yml` are upstream's.

## 9. Analytics and tracking

Done (uncommitted; builds and installs):
- Deleted modules: `components/analytics/**` (Countly metrics, Sentry
  shake2report and its "Report Bug" screen), `components/notification/**`
  (Firebase push for firmware releases), `components/wearable/**`,
  `instances/wearable`, `components/selfupdater/**`.
- Removed the `metric/impl` proto submodule, `sentry.properties`,
  `google-services.json`, the Sentry Gradle plugin, the `google-gms` plugin, and
  catalog entries for Sentry, Countly, Firebase, Play core, Play Services wearable
  and Wear Compose.
- `ApkConfig`: dropped `COUNTLY_*`, `IS_GOOGLE_FEATURE_AVAILABLE`,
  `IS_SENTRY_*`, `IS_METRIC_ENABLED`, `SOURCE_INSTALL` (+ `SourceInstall`).
- Stripped ~30 metric call sites. Metric-only code removed with them:
  `NfcEditorApi.reportUnsupportedFormat`, sync-end key counting, SubGhz region
  report, updater start/end events. `rpcstats` kept only its storage refresh on connect.
- Settings: removed Report Bug, Push Notifications toggle, Check Updates button
  and no-updates dialog, the self-updater debug switch. Version card shows `Version: x`.
- The `self_updater_debug` field stays in the settings proto so stored settings still parse.
- Also fixed: `ShareHelper` used `ApkConfig.APPLICATION_ID` for the FileProvider
  authority, which broke sharing with any id suffix; it now uses `context.packageName`.

Upstream's workflows (which built Wear OS and used Play/Sentry secrets) were
replaced in item 8.

## 10. Obtainium instead of Google Play

Play/GMS code is gone (see 9). The item 8 pipeline publishes GitHub releases plus
an Obtainium JSON; it needs the `RELEASE_*` secrets and `RELEASE_CERT_SHA256`
before the first nightly.

## 12. Unofficial fork notice

Done (uncommitted): launcher name "Flipper-Nanashi" (debug: "Flipper-Nanashi (DEBUG)");
settings version card says "Unofficial personal fork, not affiliated with Flipper Devices";
GitHub link points at the fork; README rewritten with a warning banner, differences
and build steps. README has a FieldWatch-style "Add to Obtainium" button
(`docs/assets/badge_obtainium.png`) and `docs/Flipper-Nanashi-obtainium.json`, aimed at
app id `dev.namelessnanashi.flipperzero` and asset `Flipper-Nanashi-nightly.apk` on the
`Nightly-Rolling` release. Both start working once items 7 and 8 land.
