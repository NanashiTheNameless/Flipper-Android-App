package dev.namelessnanashi.flipperzero.core.atomicfile

import okio.Path

actual fun AtomicFile(
    baseName: Path
): AtomicFile = AndroidAtomicFile(androidx.core.util.AtomicFile(baseName.toFile()))
