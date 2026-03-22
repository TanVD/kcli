package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.components.markdown
import dev.tanvd.kcli.view.model.Color
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTest {
    private fun render(content: String, color: Color? = null): String = runBlocking {
        ViewContext.Text().apply {
            markdown(content, color)
        }.get()
    }

    @Test
    fun `plain text is unchanged`() {
        val md = "Hello, world!"
        assertEquals("Hello, world!", render(md))
    }

    @Test
    fun `bold syntax is stripped to plain content`() {
        val md = "**Bold Text** and **More**"
        assertEquals("[Bold Text]:[bold] and [More]:[bold]", render(md))
    }

    @Test
    fun `italic syntax is stripped to plain content`() {
        val md = "_Italic_ and *Also Italic*"
        assertEquals("[Italic]:[italic] and [Also Italic]:[italic]", render(md))
    }

    @Test
    fun `combined emphasis`() {
        val md = "*Strong _and_ Nested*"
        assertEquals("[Strong]:[italic] [and]:[italic] [Nested]:[italic]", render(md))
    }

    @Test
    fun `line breaks in markdown become newlines in output`() {
        val md = "Line1\nLine2\n\nParagraph2".trimIndent()
        assertEquals("Line1\nLine2\n\nParagraph2", render(md))
    }

    @Test
    fun `multiple heading levels drop hashes and preserve text`() {
        val md = """
                  # Heading One
                  ## Heading Two
                  ### Heading Three
                  #### Heading Four
                  ##### Heading Five
                  ###### Heading Six
                  """.trimIndent()
        val expected = """
                  [# Heading One]:[bold]
                  [## Heading Two]:[bold]
                  [### Heading Three]:[bold]
                  [#### Heading Four]:[bold]
                  [##### Heading Five]:[bold]
                  [###### Heading Six]:[bold]
        """.trimIndent()
        assertEquals(expected, render(md))
    }

    @Test
    fun `inline code loses backticks`() {
        val md = "Use the `code()` function and `another.method()`"
        assertEquals("Use the `code()` function and `another.method()`", render(md))
    }

    @Test
    fun `link shows text and url in parentheses`() {
        val md = "[OpenAI](https://openai.com) and [Google](https://google.com)"
        assertEquals("[OpenAI](https://openai.com) and [Google](https://google.com)", render(md))
    }

    @Test
    fun `fenced code block preserves inner lines without ticks`() {
        val md = """
                   ```kotlin
                   val x = 5
                   println(x)
                   ```
                 """.trimIndent()
        assertEquals(md, render(md))
    }

    @Test
    fun `unordered list items`() {
        val md = """
                   - First item
                   - Second item
                     with continuation
                   - Third item with **bold**
                 """.trimIndent()
        val expected = """
                   - First item
                   - Second item
                     with continuation
                   - Third item with [bold]:[bold]
                 """.trimIndent()
        assertEquals(expected, render(md))
    }

    @Test
    fun `ordered list items`() {
        val md = """
                   1. First item
                   2. Second item
                   3. Third item
                 """.trimIndent()
        assertEquals(md, render(md))
    }

    @Test
    fun `empty markdown returns empty string`() {
        assertEquals("", render(""))
    }

    @Test
    fun `markdown with color parameter`() {
        val md = "**Bold text**"
        val result = render(md, Color.RED)
        assertEquals("[Bold text]:[bold, red]", result)
    }

    @Test
    fun `multiple paragraphs with proper spacing`() {
        val md = """
                  First paragraph.

                  Second paragraph with **emphasis**.

                  Third paragraph.
                """.trimIndent()
        val expected = """
                  First paragraph.

                  Second paragraph with [emphasis]:[bold].

                  Third paragraph.
                """.trimIndent()
        assertEquals(expected, render(md))
    }
}
