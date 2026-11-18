package dev.namelessnanashi.flipperzero.core.test

import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class TestFlipperStorageProvider : FlipperStorageProvider() {
    override val fileSystem: FileSystem = FakeFileSystem()
    override val rootPath: Path = "/app".toPath()
    override val tmpPath: Path = "/tmp".toPath()
}
