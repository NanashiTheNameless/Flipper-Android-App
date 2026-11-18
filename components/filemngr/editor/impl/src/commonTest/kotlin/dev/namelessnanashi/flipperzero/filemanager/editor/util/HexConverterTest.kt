package dev.namelessnanashi.flipperzero.filemanager.editor.util

import dev.namelessnanashi.flipperzero.filemanager.editor.model.HexString
import org.junit.Assert
import org.junit.Test

class HexConverterTest {
    @Test
    fun `Check encoded value and decoded is correct`() {
        val original = HexString.Text("hello world")
        val hexString = HexConverter.toHexString(original)
        Assert.assertEquals("68656C6C6F20776F726C64", hexString.content)
        val decoded = HexConverter.fromHexString(hexString)
        Assert.assertEquals(original, decoded)
    }
}
