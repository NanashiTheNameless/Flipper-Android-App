package dev.namelessnanashi.flipperzero.nfceditor.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.hexkeyboard.HexKey
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.EditorField
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcCellType
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorCell
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorCellLocation
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorLine
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorSector
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorState
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val TEST_SECTORS = 32
private const val TEST_LINES = 4
private const val TEST_CELLS = 16

class TextUpdaterHelperTest {
    private lateinit var underTest: TextUpdaterHelper

    @Before
    fun setUp() {
        underTest = TextUpdaterHelper()
        underTest.onFileLoad(
            NfcEditorState(
                sectors = List(TEST_SECTORS) { sectorIndex ->
                    NfcEditorSector(
                        List(TEST_LINES) { lineIndex ->
                            NfcEditorLine(
                                sectorIndex * TEST_LINES + lineIndex,
                                "04 77 70 2A 23 4F 80 08 44 00 12 01 11 00 27 16".split(" ")
                                    .map { NfcEditorCell(it, NfcCellType.SIMPLE) }
                                    .toImmutableList()
                            )
                        }.toImmutableList()
                    )
                }.toImmutableList()
            )
        )
    }

    @Test
    fun `on press back on start`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 0, 0, 0)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.Clear)

        Assert.assertEquals("??", underTest.getNfcEditorState().value?.get(testedLocation)?.content)
        Assert.assertNull(
            underTest.getActiveCellState().value
        )
    }

    @Test
    fun `on press back on same line`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 0, 0, 1)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.Clear)

        val nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertNotNull(nfcEditorState)
        Assert.assertEquals("??", nfcEditorState!![testedLocation].content)
        Assert.assertEquals(
            "04",
            nfcEditorState[NfcEditorCellLocation(EditorField.DATA, 0, 0, 0)].content
        )
        Assert.assertEquals(
            NfcEditorCellLocation(EditorField.DATA, 0, 0, 0),
            underTest.getActiveCellState().value
        )
    }

    @Test
    fun `on press back on another line`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 0, 1, 0)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.Clear)

        val nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertNotNull(nfcEditorState)
        Assert.assertEquals("??", nfcEditorState!![testedLocation].content)
        Assert.assertEquals(
            NfcEditorCellLocation(EditorField.DATA, 0, 0, TEST_CELLS - 1),
            underTest.getActiveCellState().value
        )
    }

    @Test
    fun `on press back on another sector`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 1, 0, 0)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.Clear)

        val nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertNotNull(nfcEditorState)
        Assert.assertEquals("??", nfcEditorState!![testedLocation].content)
        Assert.assertEquals(
            NfcEditorCellLocation(EditorField.DATA, 0, TEST_LINES - 1, TEST_CELLS - 1),
            underTest.getActiveCellState().value
        )
        Assert.assertEquals(
            NfcEditorCellLocation(EditorField.DATA, 0, TEST_LINES - 1, TEST_CELLS - 1),
            underTest.getActiveCellState().value
        )
    }

    @Test
    fun `on type text`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 0, 0, 0)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.A)

        val nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertNotNull(nfcEditorState)
        Assert.assertEquals("A", nfcEditorState!![testedLocation].content)
        Assert.assertEquals(
            testedLocation,
            underTest.getActiveCellState().value
        )
    }

    @Test
    fun `on type text and restore backup`() {
        val testedLocation = NfcEditorCellLocation(EditorField.DATA, 0, 0, 0)
        underTest.onSelectCell(testedLocation)

        underTest.onKeyboardPress(HexKey.A)

        var nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertNotNull(nfcEditorState)
        Assert.assertEquals("A", nfcEditorState!![testedLocation].content)
        Assert.assertEquals(
            testedLocation,
            underTest.getActiveCellState().value
        )
        underTest.onSelectCell(NfcEditorCellLocation(EditorField.DATA, 0, 0, 1))
        Assert.assertEquals(
            NfcEditorCellLocation(EditorField.DATA, 0, 0, 1),
            underTest.getActiveCellState().value
        )
        nfcEditorState = underTest.getNfcEditorState().value
        Assert.assertEquals("04", nfcEditorState!![testedLocation].content)
    }
}
