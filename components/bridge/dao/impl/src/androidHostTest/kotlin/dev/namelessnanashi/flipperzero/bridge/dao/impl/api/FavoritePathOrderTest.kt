package dev.namelessnanashi.flipperzero.bridge.dao.impl.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritePathOrderTest {
    private val key = FlipperKeyPath(FlipperFilePath("nfc", "Door.nfc"), false)
    private val bedroom = "subghz/remote/BedRoom.txt"
    private val workroom = "subghz/remote/WorkRoom.txt"

    @Test
    fun `remote definitions appear in their full favorites order`() {
        val result = mergeFavoritePaths(listOf(key), listOf(bedroom, key.path.pathToKey, workroom))
        assertEquals(listOf(bedroom, key.path.pathToKey, workroom), result.map { it.path.pathToKey })
        assertEquals(listOf("BedRoom", "Door", "WorkRoom"), result.map { it.path.nameWithoutExtension })
    }

    @Test
    fun `uncached favorites of any extension remain visible and newly starred keys append`() {
        val result = mergeFavoritePaths(listOf(key), listOf(workroom, "nfc/Removed.nfc", bedroom))
        assertEquals(listOf(workroom, "nfc/Removed.nfc", bedroom, key.path.pathToKey), result.map { it.path.pathToKey })
    }
    @Test
    fun `internal files app shortcuts folders and scripts keep their exact targets`() {
        val paths = listOf("/int/.region_data", "/app:setting/desktop/Lock", "apps/Scripts", "apps/Scripts/Test.js", "picopass/Door.picopass", bedroom)
        val result = mergeFavoritePaths(emptyList(), paths)
        assertEquals(paths, result.map { it.path.pathToKey })
        assertEquals(listOf("/int/.region_data", "/app:setting/desktop/Lock", "/ext/apps/Scripts", "/ext/apps/Scripts/Test.js", "/ext/picopass/Door.picopass", "/ext/$bedroom"), result.map { it.path.getPathOnFlipper() })
    }

}
