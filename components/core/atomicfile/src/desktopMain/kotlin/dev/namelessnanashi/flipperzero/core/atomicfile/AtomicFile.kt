package dev.namelessnanashi.flipperzero.core.atomicfile

import okio.Path

actual fun AtomicFile(
    baseName: Path
): AtomicFile = KmpAtomicFile(baseName)
