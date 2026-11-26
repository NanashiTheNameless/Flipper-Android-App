package dev.namelessnanashi.flipperzero.faphub.source.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FirmwareInfo
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class MomentumSdkResolverTest {
    @get:Rule val temporary = TemporaryFolder()
    private val target = FlipperTarget.Received("f7", SemVer(87, 1))
    private val hash = "a".repeat(64)
    private fun index(version: String, target: String = "f7", host: String = "up.momentum-fw.dev") =
        """{"channels":[{"id":"development","versions":[{"version":"$version","files":[{"type":"sdk_zip","target":"$target","url":"https://$host/builds/firmware/sdk-$version.zip","sha256":"$hash"}]}]}]}"""
    private fun info(revision: String) = FlipperRpcInformation(firmware = FirmwareInfo(softwareRevision = revision))
    private fun resolver(index: String) = MomentumSdkResolver(temporary.root) { Json.parseToJsonElement(index).jsonObject }

    @Test
    fun `connected development revision selects exactly its indexed SDK and checksum`() = runTest {
        val sdk = resolver(index("d3f89dfe")).resolve(info("Dev d3f89dfe"), target)
        assertEquals("d3f89dfe", sdk.version)
        assertEquals(hash, sdk.sha256)
        assertEquals("https://up.momentum-fw.dev/builds/firmware/sdk-d3f89dfe.zip", sdk.url)
    }

    @Test
    fun `stable version reported outside the display revision selects its own release`() = runTest {
        val info = info("Dev e1784e7").copy(allFields = persistentMapOf("devinfo.firmware.version" to "mntm-012"))
        assertEquals("mntm-012", resolver(index("mntm-012")).resolve(info, target).version)
    }

    @Test
    fun `legacy field spelling and full commit can match an abbreviated index revision`() = runTest {
        val info = info("unknown").copy(allFields = persistentMapOf("firmware_commit" to "d3f89dfe" + "a".repeat(32)))
        assertEquals("d3f89dfe", resolver(index("d3f89dfe")).resolve(info, target).version)
    }

    @Test
    fun `unavailable dev revision never selects the latest SDK`() = runTest {
        val result = runCatching { resolver(index("aaaaaaaa")).resolve(info("Dev d3f89dfe"), target) }
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("no longer publishes an SDK for revision d3f89dfe"))
    }

    @Test
    fun `wrong hardware SDK is not selected`() = runTest {
        assertTrue(runCatching { resolver(index("d3f89dfe", "f18")).resolve(info("Dev d3f89dfe"), target) }.isFailure)
    }

    @Test
    fun `dirty firmware cannot claim the official commit SDK`() = runTest {
        val info = info("Dev d3f89dfe").copy(allFields = persistentMapOf("devinfo.firmware.commit.dirty" to "true"))
        assertTrue(runCatching { resolver(index("d3f89dfe")).resolve(info, target) }.exceptionOrNull()?.message.orEmpty().contains("local changes"))
    }

    @Test
    fun `older stable firmware uses only its exact release tag`() = runTest {
        val requested = mutableListOf<String>()
        val resolver = MomentumSdkResolver(temporary.root) { url ->
            requested += url
            Json.parseToJsonElement(if (url == MomentumSdkResolver.INDEX) index("mntm-012") else
                """{"tag_name":"mntm-011","assets":[{"name":"flipper-z-f7-sdk-mntm-011.zip","browser_download_url":"https://github.com/Next-Flip/Momentum-Firmware/releases/download/mntm-011/flipper-z-f7-sdk-mntm-011.zip","digest":"sha256:$hash"}]}""").jsonObject
        }
        val sdk = resolver.resolve(info("e1784e7 mntm-011"), target)
        assertEquals("mntm-011", sdk.version)
        assertEquals(hash, sdk.sha256)
        assertTrue(requested.last().endsWith("/releases/tags/mntm-011"))
        assertTrue(requested.none { it.endsWith("/latest") })
    }

    @Test
    fun `indexed SDK URLs must belong to the official Momentum server`() = runTest {
        assertTrue(runCatching { resolver(index("d3f89dfe", host = "example.com")).resolve(info("Dev d3f89dfe"), target) }.isFailure)
    }
}
