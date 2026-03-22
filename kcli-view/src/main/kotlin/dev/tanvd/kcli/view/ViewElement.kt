package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.TextStyle

sealed interface ViewElement {
    data class Text(
        val content: CharSequence,
        val color: Color? = null,
        val styles: Set<TextStyle> = emptySet()
    ) : ViewElement {

        class Builder(private val content: String) {
            private var color: Color? = null
            private val styles = mutableSetOf<TextStyle>()

            fun color(color: Color?) = apply { this.color = color }
            fun bold() = apply { styles.add(TextStyle.BOLD) }
            fun faint() = apply { styles.add(TextStyle.FAINT) }
            fun italic() = apply { styles.add(TextStyle.ITALIC) }
            fun build() = Text(content, color, styles)
        }

        companion object {
            fun builder(content: String) = Builder(content)
        }
    }
    data object NewLine : ViewElement
}
