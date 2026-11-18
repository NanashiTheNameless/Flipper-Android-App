package dev.namelessnanashi.flipperzero.faphub.dao.api.model

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class FapCategory(
    val id: String,
    val name: String,
    val picUrl: String,
    val applicationCount: Int,
    @ColorInt val color: Int?
)
