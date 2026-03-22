package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.Emojis
import dev.tanvd.kcli.view.model.TextStyle

fun ViewContext.text(content: CharSequence, color: Color? = null, styles: Set<TextStyle> = emptySet()): ViewContext {
    for ((index, line) in content.lines().withIndex()) {
        element(ViewElement.Text(line, color, styles))

        if (index < content.lines().lastIndex) {
            newline()
        }
    }
    return this
}

fun ViewContext.text(content: CharSequence, builder: ViewElement.Text.Builder.() -> Unit): ViewContext {
    for ((index, line) in content.lines().withIndex()) {
        element(ViewElement.Text.Builder(line).apply(builder).build())

        if (index < content.lines().lastIndex) {
            newline()
        }
    }
    return this
}

fun ViewContext.space(): ViewContext = char(' ')

fun ViewContext.newline(): ViewContext {
    element(ViewElement.NewLine)
    return this
}

fun ViewContext.br(): ViewContext {
    newline()
    newline()
    return this
}

fun ViewContext.char(char: Char, color: Color? = null, styles: Set<TextStyle> = emptySet()): ViewContext {
    text(char.toString(), color, styles)
    return this
}

fun ViewContext.char(char: Char, builder: ViewElement.Text.Builder.() -> Unit): ViewContext {
    text(char.toString(), builder)
    return this
}

suspend fun ViewContext.padding(prefix: CharSequence, withFirstLine: Boolean = true, content: suspend ViewContext.() -> Unit): ViewContext {
    val elements = ViewContext.Container().apply { content() }.elements
    if (elements.isEmpty()) return this

    if (withFirstLine) element(ViewElement.Text(prefix))

    for ((index, element) in elements.withIndex()) {
        when (element) {
            is ViewElement.NewLine -> {
                element(element)
                if (index < elements.lastIndex) {
                    element(ViewElement.Text(prefix))
                }
            }

            is ViewElement.Text -> {
                element(element)
            }
        }
    }
    return this
}

fun ViewContext.bold(content: CharSequence, color: Color? = null): ViewContext = text(content) { bold().color(color) }

fun ViewContext.faint(content: CharSequence, color: Color? = null): ViewContext = text(content) { faint().color(color) }
fun ViewContext.italic(content: CharSequence, color: Color? = null): ViewContext = text(content) { italic().color(color) }

fun ViewContext.emoji(emojis: Emojis): ViewContext = text(emojis.symbol)

val Int.spaces: String
    get() = " ".repeat(this)
