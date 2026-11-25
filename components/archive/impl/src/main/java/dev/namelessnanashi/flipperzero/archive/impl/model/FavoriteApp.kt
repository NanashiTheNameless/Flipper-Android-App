package dev.namelessnanashi.flipperzero.archive.impl.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem

data class FavoriteApp(val name: String, val keyPath: FlipperKeyPath)

// Momentum's setting shortcuts launch the named loader app without a file argument.
// See applications/main/archive/scenes/archive_scene_browser.c in Momentum firmware.
internal val builtInFavoriteApps = listOf(
    "Sub-GHz", "125 kHz RFID", "NFC", "Infrared", "GPIO", "iButton", "Bad KB", "U2F"
).map { name ->
    FavoriteApp(name, FlipperKeyPath(FlipperFilePath.fromFavoritePath("/app:setting/$name"), false))
}

internal fun favoriteAppsFromManifests(items: List<FapManifestItem>): List<FavoriteApp> =
    (builtInFavoriteApps + items.map { item ->
        FavoriteApp(item.fullName, FlipperKeyPath(FlipperFilePath.fromFavoritePath(item.path), false))
    }).distinctBy { it.keyPath }.sortedBy { it.name.lowercase() }
