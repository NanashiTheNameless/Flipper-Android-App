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
import dev.namelessnanashi.flipperzero.keyparser.impl.parsers.impl.NFCParser
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [BuildKonfig.ROBOELECTRIC_SDK_VERSION])
class NFCParserTest {
    private val underTest = NFCParser()

    @Test
    fun `use shadow file`() = runTest {
        val flipperKey = FlipperKey(
            mainFile = FlipperFile(
                path = FlipperFilePath("test", "test.nfc"),
                content = FlipperKeyContent.RawData(readTestAsset("mf_4k.nfc"))
            ),
            additionalFiles = persistentListOf(
                FlipperFile(
                    path = FlipperFilePath("test", "test.nfc.shd"),
                    content = FlipperKeyContent.RawData(readTestAsset("mf_4k.nfc.shd"))
                )
            ),
            synchronized = false,
            deleted = false
        )

        val parsedKey = underTest.parseKey(
            flipperKey,
            FlipperFileFormat.fromFileContent(readTestAssetString("mf_4k.nfc"))
        )

        Assert.assertTrue(parsedKey is FlipperKeyParsed.NFC)
        val nfcKey = parsedKey as FlipperKeyParsed.NFC
        Assert.assertEquals("42 11", nfcKey.atqa)
        Assert.assertEquals(
            "00 00 00 00 80 08 50 00 00 00 00 00 00 00 00 00",
            nfcKey.lines[1].second
        )
        Assert.assertEquals(
            1,
            nfcKey.lines[1].first
        )
    }

    @Test
    fun `shadow file invalid`() = runTest {
        val flipperKey = FlipperKey(
            mainFile = FlipperFile(
                path = FlipperFilePath("test", "test.nfc"),
                content = FlipperKeyContent.RawData(readTestAsset("mf_4k.nfc"))
            ),
            additionalFiles = persistentListOf(
                FlipperFile(
                    path = FlipperFilePath("test", "test.nfc.invalidshd"),
                    content = FlipperKeyContent.RawData(readTestAsset("mf_4k.nfc.shd"))
                )
            ),
            synchronized = false,
            deleted = false
        )

        val parsedKey = underTest.parseKey(
            flipperKey,
            FlipperFileFormat.fromFileContent(readTestAssetString("mf_4k.nfc"))
        )

        Assert.assertTrue(parsedKey is FlipperKeyParsed.NFC)
        val nfcKey = parsedKey as FlipperKeyParsed.NFC
        Assert.assertEquals("42 00", nfcKey.atqa)
        Assert.assertEquals(
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
            nfcKey.lines[1].second
        )
        Assert.assertEquals(
            1,
            nfcKey.lines[1].first
        )
    }

    @Test
    fun `shadow file not exist`() = runTest {
        val flipperKey = FlipperKey(
            mainFile = FlipperFile(
                path = FlipperFilePath("test", "test.nfc"),
                content = FlipperKeyContent.RawData(readTestAsset("mf_4k.nfc"))
            ),
            synchronized = false,
            deleted = false
        )

        val parsedKey = underTest.parseKey(
            flipperKey,
            FlipperFileFormat.fromFileContent(readTestAssetString("mf_4k.nfc"))
        )

        Assert.assertTrue(parsedKey is FlipperKeyParsed.NFC)
        val nfcKey = parsedKey as FlipperKeyParsed.NFC
        Assert.assertEquals("42 00", nfcKey.atqa)
        Assert.assertEquals(
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00",
            nfcKey.lines[1].second
        )
        Assert.assertEquals(
            1,
            nfcKey.lines[1].first
        )
    }
}
