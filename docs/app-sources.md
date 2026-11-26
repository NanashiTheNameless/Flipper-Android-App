# App sources and backups (Beta)

Open **Tools → App sources & backups · Beta**. The workflow stays in
Flipper-Nanashi. Termux is the optional on-phone compiler dependency; importing
an existing `.fap` does not need Termux.

## Install an app

Paste a public HTTPS repository, release, `.fap`, or ZIP link and choose
**Inspect link**, or choose a local file. Release assets and archives containing
multiple apps offer a selection. Source manifests are discovered without
executing them in the Android app.

Review the source, revision, category and filename. Choose **Install app** for a
binary, or **Build & Install** for a source project. The selected Flipper must be
connected and its installed-app list and device information loaded. A different
source cannot silently overwrite an app already at the destination; choose a
different filename for that variant.

These source URL forms resolve to explicit Git commits:

```text
https://github.com/i12bp8/TagTinker
https://github.com/i12bp8/TagTinker/archive/refs/tags/v2.0.zip
https://github.com/i12bp8/TagTinker/archive/refs/heads/main.zip
```

A repository URL uses its actual default branch. Tags and branches remain
distinct, including names containing slashes. The entire source archive is
retained during compilation so shared headers outside the chosen project remain
available. Unresolved submodules and Git LFS pointers produce an error rather
than an incomplete build or an automatic bootstrap-script execution.

## Builder setup

The Beta compiler supports ARM64 Android phones and standard uFBT external C/C++
projects with one literal application ID per selected `application.fam`.

1. Install and initialize [Termux](https://github.com/termux/termux-app/releases).
2. Expand **Builder setup**, copy the setup command and paste it into Termux once.
   It enables external commands and installs Python, uFBT 0.2.6, build
   dependencies and the glibc compatibility runtime.
3. Grant Flipper-Nanashi **Run commands in Termux** access, then choose
   **Check builder setup**. The result appears in the tool, including missing
   dependencies or Termux configuration errors.
4. Enable trusted-source builds. Flipper manifests execute Python in Termux;
   build only sources you trust with access to that Termux environment.
5. Leave the SDK override blank to select the Momentum SDK matching the exact
   release or development revision on the connected Flipper. The official
   Momentum firmware index supplies the SDK URL and SHA-256; older stable
   releases can use their exact tagged GitHub release. An unavailable development
   revision or locally modified firmware requires its own matching SDK ZIP.
   The SDK hash, firmware identity and source revision are pinned for the job;
   another revision or the latest SDK is never substituted.

There are no terminal commands or manual file transfers for subsequent builds.
Flipper-Nanashi launches a versioned worker through Termux's documented
[RUN_COMMAND interface](https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent).
An authenticated, job-specific loopback connection carries source/SDK inputs,
logs and the output binary, avoiding Android's intent-result size limit and
shared-storage permissions. The compiler compatibility approach follows the
[published Android FAP build guide](https://github.com/DarkFlippers/unleashed-firmware/blob/dev/documentation/BuildingFapsOnAndroid.md).

Builds have a foreground notification and cancellation. A failed build cannot
reuse a stale `dist/` binary. Interrupted operations are recorded and can be
retried. Android/Termux process termination can still interrupt work; this is not
a guarantee that the OS will let every build complete in the background.

## Links and updates

**Share link** creates a URL such as:

```text
flipper-nanashi://app?url=https%3A%2F%2Fgithub.com%2Fi12bp8%2FTagTinker
```

An optional, encoded `project` parameter selects a project within a repository.
Opening a link fills the review screen; it does not run build scripts or install
an app automatically. Android sharing of HTTPS text, FAP files and ZIPs is also
supported.

**Check for updates** follows the recorded branch or release policy. A branch
does not silently switch when a repository changes its default branch. Pinned
tags remain pinned. Direct downloads are checked by content hash, including
rolling assets replaced under the same name. Local files have no remote update
source. **Review update** creates a separate review job and preserves the app's
destination and source ownership.

External installations store provenance both in the phone's persistent records
and in namespaced fields in the Flipper installation manifest. This includes
source URL, revision, project, SDK URL/hash, firmware and artifact hashes.
Official catalog update and repair paths exclude external-source apps.

FAP inspection bounds-checks ELF/metadata, target and API before transfer. All
installs use the existing serialized upload queue and transfer verification;
device identity and firmware are checked again before finalization. API matches
are a compatibility screen, not proof that every fork-specific imported symbol
or runtime behavior is supported by the device loader.

## Import and export

**Export backup** writes a versioned ZIP using Android's document picker. A
complete installed-app backup requires the connected Flipper so catalog and
other non-rebuildable binaries can be read. Existing key-only exports remain
available in Settings.

Backups contain:

- Keys, additional files, notes and deleted-key state.
- User settings, full favorites order, hidden catalog apps and saved device
  descriptions/addresses.
- Portable widget presets. Android requires widgets to be placed again on a new
  phone; stale launcher widget IDs are not imported.
- App installation metadata and source/build recipes, including pinned source
  and SDK hashes.
- Local source archives that cannot be fetched again.
- FAP binaries for apps without a reproducible source recipe. Source-built
  binaries are omitted when the recipe or retained source snapshot can rebuild
  them. Catalog ownership is preserved when restoring catalog binaries.

Import validates metadata, paths, file hashes and artifacts before applying user
data. Matching keys are updated transactionally while preserving their database
identities; unrelated existing keys remain. Settings and favorites are replaced
by the backed-up values. Saved BLE devices are merged without changing the active
connection; Android Bluetooth pairing may still be required on another phone.
USB device paths are discovered again rather than copied to a different phone.
An imported backup cannot enable trusted-source execution by itself.

The import dialog lets you restore/rebuild apps on the connected Flipper or just
import local data and recipes. Rebuilding requires completed builder setup and
a matching firmware SDK. Retained app binaries and recipes remain available in
the tool when app restoration is postponed.

This is a backup of Flipper-Nanashi's managed data and app artifacts, not an image
of the entire Flipper SD card or the device's firmware/runtime data directories.

## Cleanup and limits

Successful installs delete temporary downloads, unpacked source trees, SDK ZIPs
and intermediate outputs. Termux deletes each build workspace and keeps at most
three SDK profiles plus its reusable compiler cache. Binaries without a rebuild
recipe and irreplaceable local source snapshots are retained for backups.
Source-specific object files, debug ELFs and duplicate FAPs in the SDK build
directory are removed too. Startup retries cleanup for completed installations.
Failed jobs retain retry inputs and bounded logs. Backup staging directories are
removed when the operation ends; the user-selected exported backup is kept.
Catalog bundles are released after successful installs/updates and abandoned
prefetches. Other abandoned bundle files expire after 24 hours on subsequent
downloads; current downloads are protected. Manifest caches prune obsolete
entries, and firmware-update temporary folders are removed when their job ends.
Builder setup disables the pip download cache and cleans package archives while
keeping the installed build tools.

The first Beta supports HTTPS, ZIP and standard uFBT projects. Private repository
authentication, arbitrary build systems, unresolved dependencies, automatic
plugin/side-asset pack installation, TGZ firmware resource extraction and
dedicated third-party catalog browsing are not implemented. FlipC is excluded.
No import action flashes firmware.
