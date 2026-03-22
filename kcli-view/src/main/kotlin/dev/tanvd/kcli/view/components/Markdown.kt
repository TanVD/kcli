package dev.tanvd.kcli.view.components

import dev.tanvd.kcli.view.ViewContext
import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.TextStyle
import dev.tanvd.kcli.view.newline
import dev.tanvd.kcli.view.text
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

fun ViewContext.markdown(content: String, color: Color? = null): ViewContext {
    val parser = MarkdownParser(CommonMarkFlavourDescriptor())
    val root = parser.buildMarkdownTreeFromString(content)
    renderMarkdown(root, content, color)
    return this
}

private fun ViewContext.renderMarkdown(
    node: ASTNode,
    original: String,
    color: Color?,
    styles: Set<TextStyle> = emptySet()
): ViewContext {
    when (node.type) {
        MarkdownElementTypes.ATX_1,
        MarkdownElementTypes.ATX_2,
        MarkdownElementTypes.ATX_3,
        MarkdownElementTypes.ATX_4,
        MarkdownElementTypes.ATX_5,
        MarkdownElementTypes.ATX_6 -> {
            text(node.getTextInNode(original), color, setOf(TextStyle.BOLD))
        }

        MarkdownElementTypes.EMPH, MarkdownElementTypes.STRONG -> {
            val additional = when (node.type) {
                MarkdownElementTypes.EMPH -> setOf(TextStyle.ITALIC)
                MarkdownElementTypes.STRONG -> setOf(TextStyle.BOLD)
                else -> emptySet()
            }
            val toDrop = when (node.type) {
                MarkdownElementTypes.EMPH -> 1
                MarkdownElementTypes.STRONG -> 2
                else -> 0
            }
            for (child in node.children.drop(toDrop).dropLast(toDrop)) {
                renderMarkdown(child, original, color, styles + additional)
            }
        }

        MarkdownTokenTypes.EOL -> newline()

        else -> {
            if (node.children.isEmpty()) {
                text(node.getTextInNode(original), color, styles)
            } else {
                for (child in node.children) {
                    renderMarkdown(child, original, color, styles)
                }
            }
        }
    }

    return this
}
