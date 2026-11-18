package dev.namelessnanashi.flipperzero.faphub.installation.button.api

import androidx.compose.runtime.Immutable
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion

@Immutable
data class FapButtonConfig(
    val applicationUid: String,
    val applicationAlias: String,
    val iconUrl: String,
    val version: FapItemVersion,
    val categoryAlias: String,
    val applicationName: String
)

fun FapItemShort.toFapButtonConfig() = FapButtonConfig(
    applicationAlias = applicationAlias,
    version = upToDateVersion,
    applicationUid = id,
    categoryAlias = category.name,
    applicationName = name,
    iconUrl = picUrl
)

fun FapItem.toFapButtonConfig() = FapButtonConfig(
    applicationAlias = applicationAlias,
    version = upToDateVersion,
    applicationUid = id,
    categoryAlias = category.name,
    applicationName = name,
    iconUrl = picUrl
)
