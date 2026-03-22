package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.Emojis
import dev.tanvd.kcli.view.model.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewContentTest {
    private fun assertTerminalText(expected: String, color: Color? = null, styles: Set<TextStyle> = emptySet(), actual: ViewElement) {
        require(actual is ViewElement.Text) { "Expected Text element but got $actual" }
        assertEquals(expected, actual.content)
        assertEquals(color, actual.color)
        assertEquals(styles, actual.styles)
    }

    @Test
    fun `test text without color`() {
        val content = ViewContext.Container().apply {
            text("Hello")
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("Hello", actual = content[0])
    }

    @Test
    fun `test text with color`() {
        val content = ViewContext.Container().apply {
            text("Hello", Color.RED)
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("Hello", Color.RED, actual = content[0])
    }

    @Test
    fun `test text with styles`() {
        val content = ViewContext.Container().apply {
            text("Hello", styles = setOf(TextStyle.BOLD, TextStyle.ITALIC))
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("Hello", styles = setOf(TextStyle.BOLD, TextStyle.ITALIC), actual = content[0])
    }

    @Test
    fun `test styled text using builder`() {
        val content = ViewContext.Container().apply {
            text("Hello") {
                bold()
                color(Color.RED)
                italic()
            }
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("Hello", Color.RED, setOf(TextStyle.BOLD, TextStyle.ITALIC), actual = content[0])
    }

    @Test
    fun `test char without color`() {
        val content = ViewContext.Container().apply {
            char('A')
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("A", actual = content[0])
    }

    @Test
    fun `test char with color`() {
        val content = ViewContext.Container().apply {
            char('A', Color.BLUE)
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("A", Color.BLUE, actual = content[0])
    }

    @Test
    fun `test styled char`() {
        val content = ViewContext.Container().apply {
            char('A') {
                bold()
                color(Color.BLUE)
            }
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("A", Color.BLUE, setOf(TextStyle.BOLD), actual = content[0])
    }

    @Test
    fun `test newLine`() {
        val content = ViewContext.Container().apply {
            newline()
        }.elements

        assertEquals(1, content.size)
        assertEquals(ViewElement.NewLine, content[0])
    }

    @Test
    fun `test convenience styling methods`() {
        val content = ViewContext.Container().apply {
            bold("Bold")
            newline()
            italic("Italic")
            newline()
            text("Colored", Color.RED)
        }.elements

        assertEquals(5, content.size)
        assertTerminalText("Bold", styles = setOf(TextStyle.BOLD), actual = content[0])
        assertEquals(ViewElement.NewLine, content[1])
        assertTerminalText("Italic", styles = setOf(TextStyle.ITALIC), actual = content[2])
        assertEquals(ViewElement.NewLine, content[3])
        assertTerminalText("Colored", Color.RED, actual = content[4])
    }

    @Test
    fun `test infix plus operation`() {
        val content1 = ViewContext.Container().apply { text("Hello") }.elements
        val content2 = ViewContext.Container().apply { text("World"); newline() }.elements

        val result = (content1 + content2)

        assertEquals(3, result.size)
        assertTerminalText("Hello", actual = result[0])
        assertTerminalText("World", actual = result[1])
        assertEquals(ViewElement.NewLine, result[2])
    }

    @Test
    fun `test emoji extension`() {
        val content = ViewContext.Container().apply {
            emoji(Emojis.CHECK_MARK)
        }.elements

        assertEquals(1, content.size)
        assertTerminalText("✅", actual = content[0])
    }
}
