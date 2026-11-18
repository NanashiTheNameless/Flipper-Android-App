package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.util

import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandModel

fun BrandModel.charSection(): Char {
    val ch = name.first()
    return when {
        ch.isDigit() -> '#'
        ch.isLetter() -> ch.uppercaseChar()
        else -> '#'
    }
}
