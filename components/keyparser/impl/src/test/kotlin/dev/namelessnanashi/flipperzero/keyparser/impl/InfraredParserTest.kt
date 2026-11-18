package dev.namelessnanashi.flipperzero.keyparser.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.test.readTestAsset
import dev.namelessnanashi.flipperzero.core.test.readTestAssetString
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyparser.impl.parsers.impl.InfraredParser
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [BuildKonfig.ROBOELECTRIC_SDK_VERSION])
class InfraredParserTest {
    private val underTest = InfraredParser()

    @Test
    fun `valid infrared key`() = runTest {
        val listRemotes = listOf("Button_1", "Button_2", "Button_3")
        val flipperKey = FlipperKey(
            mainFile = FlipperFile(
                path = FlipperFilePath("test", "example.ir"),
                content = FlipperKeyContent.RawData(readTestAsset("example.ir"))
            ),
            synchronized = false,
            deleted = false
        )

        val parsedKey = underTest.parseKey(
            flipperKey,
            FlipperFileFormat.fromFileContent(readTestAssetString("example.ir"))
        )

        Assert.assertTrue(parsedKey is FlipperKeyParsed.Infrared)

        val parsedInfrared = parsedKey as FlipperKeyParsed.Infrared

        Assert.assertArrayEquals(listRemotes.toTypedArray(), parsedInfrared.remotes.toTypedArray())
        Assert.assertEquals("example", parsedInfrared.keyName)
    }
}
