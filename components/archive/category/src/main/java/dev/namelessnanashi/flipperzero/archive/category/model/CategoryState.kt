package dev.namelessnanashi.flipperzero.archive.category.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import kotlinx.collections.immutable.ImmutableList

sealed class CategoryState {
    data object Loading : CategoryState()

    data class Loaded(
        val keys: ImmutableList<Pair<FlipperKeyParsed, FlipperKey>>
    ) : CategoryState()
}
