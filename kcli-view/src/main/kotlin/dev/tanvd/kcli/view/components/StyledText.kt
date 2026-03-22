package dev.tanvd.kcli.view.components

import dev.tanvd.kcli.view.ViewContext
import dev.tanvd.kcli.view.components.StyledTextParser.Token
import dev.tanvd.kcli.view.components.StyledTextParser.tokenize
import dev.tanvd.kcli.view.emoji
import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.Emojis
import dev.tanvd.kcli.view.model.TextStyle
import dev.tanvd.kcli.view.text

/**
 * Enhanced parser for styled text that supports:
 * - **bold**, *italic* markdown syntax
 * - [text]:[style1,style2,color:red] for multiple styles and colors
 * - :emoji_id: for emojis (Discord-style)
 * - {{emoji_id}} as alternative emoji syntax
 * - Escaped characters with backslash
 */
internal object StyledTextParser {
    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0

        while (i < input.length) {
            when {
                input[i] == '\\' && i + 1 < input.length -> {
                    tokens.add(Token.PlainText(input[i + 1].toString()))
                    i += 2
                }

                input[i] == '[' -> {
                    val styledToken = parseStyledText(input, i)
                    if (styledToken != null) {
                        tokens.add(styledToken.first)
                        i = styledToken.second
                    } else {
                        tokens.add(Token.PlainText(input[i].toString()))
                        i++
                    }
                }

                input[i] == ':' -> {
                    val emojiToken = parseDiscordEmoji(input, i)
                    if (emojiToken != null) {
                        tokens.add(emojiToken.first)
                        i = emojiToken.second
                    } else {
                        tokens.add(Token.PlainText(input[i].toString()))
                        i++
                    }
                }

                input.startsWith("{{", i) -> {
                    val emojiToken = parseBraceEmoji(input, i)
                    if (emojiToken != null) {
                        tokens.add(emojiToken.first)
                        i = emojiToken.second
                    } else {
                        tokens.add(Token.PlainText("{"))
                        i++
                    }
                }

                input.startsWith("**", i) -> {
                    val boldToken = parseMarkdownLike(input, i, "**", setOf(TextStyle.BOLD))
                    if (boldToken != null) {
                        tokens.add(boldToken.first)
                        i = boldToken.second
                    } else {
                        tokens.add(Token.PlainText("*"))
                        i++
                    }
                }

                input[i] == '*' -> {
                    val italicToken = parseMarkdownLike(input, i, "*", setOf(TextStyle.ITALIC))
                    if (italicToken != null) {
                        tokens.add(italicToken.first)
                        i = italicToken.second
                    } else {
                        tokens.add(Token.PlainText("*"))
                        i++
                    }
                }

                else -> {
                    tokens.add(Token.PlainText(input[i].toString()))
                    i++
                }
            }
        }

        return mergePlainTextTokens(tokens)
    }

    private fun parseStyledText(input: String, startIndex: Int): Pair<Token.StyledText, Int>? {
        val textEndIndex = input.indexOf("]:[", startIndex)
        if (textEndIndex == -1) return null

        val styleEndIndex = input.indexOf(']', textEndIndex + 3)
        if (styleEndIndex == -1) return null

        val text = input.substring(startIndex + 1, textEndIndex)
        val styleString = input.substring(textEndIndex + 3, styleEndIndex)

        val (styles, color) = parseStyles(styleString)

        return Token.StyledText(text, color, styles) to styleEndIndex + 1
    }

    private fun parseStyles(styleString: String): Pair<Set<TextStyle>, Color?> {
        val styles = mutableSetOf<TextStyle>()
        var color: Color? = null

        val parts = styleString.split(',').map { it.trim() }

        for (part in parts) {
            when {
                part.startsWith("color:") -> {
                    val colorName = part.substring(6)
                    color = Color.entries.find { it.name.equals(colorName, ignoreCase = true) }
                }

                else -> {
                    val style = TextStyle.entries.find { it.name.equals(part, ignoreCase = true) }
                    if (style != null) {
                        styles.add(style)
                    }
                }
            }
        }

        return styles to color
    }

    private fun parseDiscordEmoji(input: String, startIndex: Int): Pair<Token.Emoji, Int>? {
        val endIndex = input.indexOf(':', startIndex + 1)
        if (endIndex == -1) return null

        val emojiName = input.substring(startIndex + 1, endIndex)
        if (emojiName.isEmpty()) return null

        val emoji = Emojis.entries.find { it.name.equals(emojiName, ignoreCase = true) }
            ?: return null

        return Token.Emoji(emoji) to endIndex + 1
    }

    private fun parseBraceEmoji(input: String, startIndex: Int): Pair<Token.Emoji, Int>? {
        val endIndex = input.indexOf("}}", startIndex + 2)
        if (endIndex == -1) return null

        val emojiName = input.substring(startIndex + 2, endIndex)
        if (emojiName.isEmpty()) return null

        val emoji = Emojis.entries.find { it.name.equals(emojiName, ignoreCase = true) }
            ?: return null

        return Token.Emoji(emoji) to endIndex + 2
    }

    private fun parseMarkdownLike(
        input: String,
        startIndex: Int,
        delimiter: String,
        styles: Set<TextStyle>
    ): Pair<Token.StyledText, Int>? {
        val endIndex = input.indexOf(delimiter, startIndex + delimiter.length)
        if (endIndex == -1) return null

        val content = input.substring(startIndex + delimiter.length, endIndex)
        if (content.isEmpty()) return null

        return Token.StyledText(content, null, styles) to endIndex + delimiter.length
    }

    private fun mergePlainTextTokens(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens

        val merged = mutableListOf<Token>()
        var currentPlainText = StringBuilder()

        for (token in tokens) {
            if (token is Token.PlainText) {
                currentPlainText.append(token.content)
            } else {
                if (currentPlainText.isNotEmpty()) {
                    merged.add(Token.PlainText(currentPlainText.toString()))
                    currentPlainText.clear()
                }
                merged.add(token)
            }
        }

        if (currentPlainText.isNotEmpty()) {
            merged.add(Token.PlainText(currentPlainText.toString()))
        }

        return merged
    }

    internal sealed class Token {
        data class PlainText(val content: String) : Token()
        data class StyledText(val content: String, val color: Color?, val styles: Set<TextStyle>) : Token()
        data class Emoji(val emoji: Emojis) : Token()
    }
}


fun ViewContext.styled(input: String): ViewContext {
    val tokens = tokenize(input)

    for (token in tokens) {
        when (token) {
            is Token.PlainText -> text(token.content)
            is Token.StyledText -> text(token.content, token.color, token.styles)
            is Token.Emoji -> emoji(token.emoji)
        }
    }

    return this
}
