package dev.tanvd.kcli.view.impl

import dev.tanvd.kcli.view.ViewElement
import dev.tanvd.kcli.view.model.Color
import dev.tanvd.kcli.view.model.TextStyle

object TerminalEscapeSequences {

    private const val ESC = "\u001B"
    private const val RESET = "$ESC[0m"

    fun styled(text: ViewElement.Text): CharSequence {
        if (text.color == null && text.styles.isEmpty()) {
            return text.content
        }

        val codes = mutableListOf<String>()

        text.color?.let { color ->
            val palette = Color.Palette.current
            val rgb = palette.color(color)
            codes.add("38;2;${rgb.r};${rgb.g};${rgb.b}")
        }

        text.styles.forEach { textStyle ->
            when (textStyle) {
                TextStyle.BOLD -> codes.add("1")
                TextStyle.ITALIC -> codes.add("3")
                TextStyle.FAINT -> codes.add("2")
            }
        }

        return if (codes.isNotEmpty()) {
            "$ESC[${codes.joinToString(";")}m${text.content}$RESET"
        } else {
            text.content
        }
    }
}
