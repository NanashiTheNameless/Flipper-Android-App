package dev.namelessnanashi.flipperzero.archive.search.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import kotlinx.collections.immutable.ImmutableList

sealed class SearchState {
    object Loading : SearchState()

    class Loaded(
        val keys: ImmutableList<Pair<FlipperKeyParsed, FlipperKey>>
    ) : SearchState()
}
