package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import org.junit.Assert.*
import org.junit.Test

class ExternalSourceManifestTest {
    @Test
    fun `external provenance survives a portable manifest round trip without injecting fields`() {
        val source = ExternalAppSource("https://github.com/u/r?text=a:b\nUID: forged", "abc", "branch:feature/app", "a".repeat(64),
            "apps/test", "https://example.com/sdk.zip", "b".repeat(64), "mntm-012", "termux-ufbt", "c".repeat(64))
        val manifest = FapManifestItem("external_app", "external:" + "d".repeat(64), "sha", "/ext/apps/Tools/app.fap", "App", null, SemVer(88, 2), isDevCatalog = false, externalSource = source)
        val parser = FapManifestParser()
        val encoded = parser.encode(manifest).openStream().readBytes()
        val decoded = requireNotNull(parser.parse(encoded, "external_app.fim"))
        assertEquals(manifest.uid, decoded.uid)
        assertEquals(source, decoded.externalSource)
        assertFalse(encoded.decodeToString().contains("UID: forged"))
    }

    @Test
    fun `existing catalog manifests retain catalog ownership`() {
        val bytes = "UID: catalog\nVersion UID: version\nPath: /ext/apps/Tools/app.fap\n".toByteArray()
        assertNull(FapManifestParser().parse(bytes, "app.fim")!!.externalSource)
    }
}
