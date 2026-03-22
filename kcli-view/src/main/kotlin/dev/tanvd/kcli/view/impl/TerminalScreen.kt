package dev.tanvd.kcli.view.impl

import dev.tanvd.kcli.view.ViewElement

object TerminalScreen {
    private val writer = System.out.writer()

    fun print(element: ViewElement) {
        writer.append(string(element))
        writer.flush()
    }

    fun string(element: ViewElement): CharSequence = when (element) {
        is ViewElement.Text -> TerminalEscapeSequences.styled(element)
        ViewElement.NewLine -> "\n"
    }
}
