package dev.tanvd.kcli.view

suspend fun print(content: suspend ViewContext.() -> Unit) {
    ViewContext.Printer.content()
    ViewContext.Printer.newline()
}

suspend fun container(content: suspend ViewContext.() -> Unit): ViewContext.Container {
    val container = ViewContext.Container()
    container.content()
    return container
}
