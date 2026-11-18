package dev.namelessnanashi.flipperzero.faphub.dao.network.network.model

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.utils.DateSerializer
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KtorfitApplicationShort(
    @SerialName("_id") val id: String,
    @SerialName("created_at")
    @Serializable(with = DateSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("updated_at")
    @Serializable(with = DateSerializer::class)
    val updatedAt: LocalDateTime,
    @SerialName("category_id") val categoryId: String,
    @SerialName("alias") val alias: String,
    @SerialName("current_version") val currentVersion: KtorfitCurrentVersionShort,
) {
    fun toFapItemShort(category: FapCategory?, target: FlipperTarget): FapItemShort? {
        if (category == null) {
            return null
        }
        val fapItemVersion = FapItemVersion(
            id = currentVersion.id,
            version = SemVer.fromString(currentVersion.version)
                ?: error("Can't parse ${currentVersion.version}"),
            target = target,
            buildState = currentVersion.status.toFapBuildState(target),
            sdkApi = null
        )

        return FapItemShort(
            id = id,
            picUrl = currentVersion.iconUrl,
            shortDescription = currentVersion.shortDescription,
            name = currentVersion.name,
            category = category,
            screenshots = currentVersion.screenshots.toImmutableList(),
            applicationAlias = alias,
            upToDateVersion = fapItemVersion
        )
    }
}
