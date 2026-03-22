package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.components.StyledTextParser.Token
import dev.tanvd.kcli.view.components.StyledTextParser.tokenize
import dev.tanvd.kcli.view.components.styled
import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.Emojis
import dev.tanvd.kcli.view.model.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StyledTextParserTest {

    private fun assertPlainTextToken(expected: String, actual: Token) {
        assertIs<Token.PlainText>(actual)
        assertEquals(expected, actual.content)
    }

    private fun assertStyledTextToken(
        expectedContent: String,
        expectedColor: Color? = null,
        expectedStyles: Set<TextStyle> = emptySet(),
        actual: Token
    ) {
        assertIs<Token.StyledText>(actual)
        assertEquals(expectedContent, actual.content)
        assertEquals(expectedColor, actual.color)
        assertEquals(expectedStyles, actual.styles)
    }

    private fun assertEmojiToken(expected: Emojis, actual: Token) {
        assertIs<Token.Emoji>(actual)
        assertEquals(expected, actual.emoji)
    }

    private fun assertViewElementText(
        expectedContent: String,
        expectedColor: Color? = null,
        expectedStyles: Set<TextStyle> = emptySet(),
        actual: ViewElement
    ) {
        assertIs<ViewElement.Text>(actual)
        assertEquals(expectedContent, actual.content)
        assertEquals(expectedColor, actual.color)
        assertEquals(expectedStyles, actual.styles)
    }

    @Test
    fun `test tokenize plain text`() {
        val tokens = tokenize("Hello World")
        assertEquals(1, tokens.size)
        assertPlainTextToken("Hello World", tokens[0])
    }

    @Test
    fun `test tokenize empty string`() {
        val tokens = tokenize("")
        assertEquals(0, tokens.size)
    }

    @Test
    fun `test tokenize bold text`() {
        val tokens = tokenize("**bold**")
        assertEquals(1, tokens.size)
        assertStyledTextToken("bold", expectedStyles = setOf(TextStyle.BOLD), actual = tokens[0])
    }

    @Test
    fun `test tokenize italic text`() {
        val tokens = tokenize("*italic*")
        assertEquals(1, tokens.size)
        assertStyledTextToken("italic", expectedStyles = setOf(TextStyle.ITALIC), actual = tokens[0])
    }

    @Test
    fun `test tokenize bold and italic mixed`() {
        val tokens = tokenize("**bold** and *italic*")
        assertEquals(3, tokens.size)
        assertStyledTextToken("bold", expectedStyles = setOf(TextStyle.BOLD), actual = tokens[0])
        assertPlainTextToken(" and ", tokens[1])
        assertStyledTextToken("italic", expectedStyles = setOf(TextStyle.ITALIC), actual = tokens[2])
    }

    @Test
    fun `test tokenize advanced styling with color`() {
        val tokens = tokenize("[colored text]:[color:red]")
        assertEquals(1, tokens.size)
        assertStyledTextToken("colored text", Color.RED, actual = tokens[0])
    }

    @Test
    fun `test tokenize advanced styling with single style`() {
        val tokens = tokenize("[bold text]:[bold]")
        assertEquals(1, tokens.size)
        assertStyledTextToken("bold text", expectedStyles = setOf(TextStyle.BOLD), actual = tokens[0])
    }

    @Test
    fun `test tokenize advanced styling with multiple styles`() {
        val tokens = tokenize("[styled text]:[bold,italic]")
        assertEquals(1, tokens.size)
        assertStyledTextToken("styled text", expectedStyles = setOf(TextStyle.BOLD, TextStyle.ITALIC), actual = tokens[0])
    }

    @Test
    fun `test tokenize discord emoji`() {
        val tokens = tokenize(":check_mark:")
        assertEquals(1, tokens.size)
        assertEmojiToken(Emojis.CHECK_MARK, tokens[0])
    }

    @Test
    fun `test tokenize brace emoji`() {
        val tokens = tokenize("{{check_mark}}")
        assertEquals(1, tokens.size)
        assertEmojiToken(Emojis.CHECK_MARK, tokens[0])
    }

    @Test
    fun `test tokenize escaped asterisk`() {
        val tokens = tokenize("\\*not italic\\*")
        assertEquals(1, tokens.size)
        assertPlainTextToken("*not italic*", tokens[0])
    }

    @Test
    fun `test tokenize unclosed bold`() {
        val tokens = tokenize("**unclosed bold")
        assertEquals(1, tokens.size)
        assertPlainTextToken("**unclosed bold", tokens[0])
    }

    @Test
    fun `test tokenize invalid emoji name`() {
        val tokens = tokenize(":invalid_emoji:")
        assertEquals(1, tokens.size)
        assertPlainTextToken(":invalid_emoji:", tokens[0])
    }

    @Test
    fun `test tokenize complex mixed content`() {
        val tokens = tokenize("Hello **bold** :check_mark: *italic* [colored]:[color:red] {{check_mark}}")
        assertPlainTextToken("Hello ", tokens[0])
        assertStyledTextToken("bold", expectedStyles = setOf(TextStyle.BOLD), actual = tokens[1])
        assertPlainTextToken(" ", tokens[2])
        assertEmojiToken(Emojis.CHECK_MARK, tokens[3])
        assertPlainTextToken(" ", tokens[4])
        assertStyledTextToken("italic", expectedStyles = setOf(TextStyle.ITALIC), actual = tokens[5])
        assertPlainTextToken(" ", tokens[6])
        assertStyledTextToken("colored", Color.RED, actual = tokens[7])
        assertPlainTextToken(" ", tokens[8])
        assertEmojiToken(Emojis.CHECK_MARK, tokens[9])
    }

    @Test
    fun `test styled function with plain text`() {
        val elements = ViewContext.Container().apply {
            styled("Hello World")
        }.elements
        assertEquals(1, elements.size)
        assertViewElementText("Hello World", actual = elements[0])
    }

    @Test
    fun `test styled function with bold text`() {
        val elements = ViewContext.Container().apply {
            styled("**bold**")
        }.elements
        assertEquals(1, elements.size)
        assertViewElementText("bold", expectedStyles = setOf(TextStyle.BOLD), actual = elements[0])
    }

    @Test
    fun `test styled function with emoji`() {
        val elements = ViewContext.Container().apply {
            styled(":check_mark:")
        }.elements
        assertEquals(1, elements.size)
        assertViewElementText(Emojis.CHECK_MARK.symbol, actual = elements[0])
    }

    @Test
    fun `test styled function with complex mixed content`() {
        val elements = ViewContext.Container().apply {
            styled("Hello **bold** :check_mark:")
        }.elements
        assertEquals(4, elements.size)
        assertViewElementText("Hello ", actual = elements[0])
        assertViewElementText("bold", expectedStyles = setOf(TextStyle.BOLD), actual = elements[1])
        assertViewElementText(" ", actual = elements[2])
        assertViewElementText(Emojis.CHECK_MARK.symbol, actual = elements[3])
    }
}
