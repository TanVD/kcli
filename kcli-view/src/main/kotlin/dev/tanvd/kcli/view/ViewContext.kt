package dev.tanvd.kcli.view

import dev.tanvd.kcli.view.impl.TerminalScreen

interface ViewContext {
    fun element(element: ViewElement)

    fun elements(elements: List<ViewElement>) = elements.forEach(::element)

    data class Container(val elements: MutableList<ViewElement> = mutableListOf()) : ViewContext {
        override fun element(element: ViewElement) {
            elements.add(element)
        }
    }

    object Printer : ViewContext {
        override fun element(element: ViewElement) {
            TerminalScreen.print(element)
        }
    }

    class Text : ViewContext {
        private val builder = StringBuilder()

        override fun element(element: ViewElement) {
            when (element) {
                is ViewElement.NewLine -> {
                    builder.appendLine()
                }

                is ViewElement.Text -> {
                    if (element.styles.isEmpty() && element.color == null || element.content.isBlank()) {
                        builder.append(element.content)
                    } else {
                        builder.append("[${element.content}]")
                        builder.append(":")
                        builder.append(element.styles.map { it.id } + (element.color?.let { listOf(it.id) }
                            ?: emptyList()).sorted())
                    }
                }
            }
        }

        fun get() = builder.toString()
    }
}
