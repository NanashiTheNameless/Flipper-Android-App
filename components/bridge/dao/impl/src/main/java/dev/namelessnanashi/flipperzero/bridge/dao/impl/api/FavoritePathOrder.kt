package dev.namelessnanashi.flipperzero.bridge.dao.impl.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath

/** Full paths remain authoritative even when a file has no cached key or recognized extension. */
internal fun mergeFavoritePaths(known: List<FlipperKeyPath>, stored: List<String>): List<FlipperKeyPath> {
    val knownByPath = known.associateBy { it.path.pathToKey }
    val ordered = stored.distinct().filter { it.isNotBlank() }.map { path ->
        knownByPath[path] ?: FlipperKeyPath(FlipperFilePath.fromFavoritePath(path), false)
    }
    val included = ordered.map { it.path.pathToKey }.toSet()
    return ordered + known.filterNot { it.path.pathToKey in included }
}
