package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.*
import dev.tanvd.kcli.view.ViewContext
import dev.tanvd.kcli.view.br
import dev.tanvd.kcli.view.italic
import dev.tanvd.kcli.view.newline
import dev.tanvd.kcli.view.space
import dev.tanvd.kcli.view.text
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class CommandWithProperties : Command {
    private val lock = Mutex()

    protected val properties = mutableListOf<CommandProperty<*>>()
    private val descriptions = mutableMapOf<String, String>()

    fun <T> property(description: String, getter: CommandArgsReader<CommandArgs, T>): CommandProperty<T> {
        val prop = CommandProperty(getter, description)
        properties.add(prop)
        return prop
    }

    fun <T> namedProperty(
        name: String,
        description: String,
        build: CommandArgsReader<CommandArgs, String?>.() -> CommandArgsReader<CommandArgs, T>
    ): CommandProperty<T> {
        descriptions[name] = description
        return property(description, named(name).build())
    }

    fun <T> namedOrPositionalProperty(
        name: String,
        index: Int = 0,
        description: String,
        build: CommandArgsReader<CommandArgs, String?>.() -> CommandArgsReader<CommandArgs, T>
    ): CommandProperty<T> {
        descriptions[name] = description
        return property(description, namedOrPositional(name, index).build())
    }

    override suspend fun check(args: CommandArgs): Boolean = true

    override suspend fun run(args: CommandArgs) = lock.withLock {
        properties.forEach { it.fillFrom(args) }
        execute()
        properties.forEach { it.reset() }
    }

    abstract suspend fun execute()

    abstract val description: String

    override suspend fun describe(context: ViewContext) {
        context.apply {
            for ((name, description) in descriptions) {
                italic("--$name").text(":").space().text(description)
                newline()
            }
            text(description)
            newline()
        }
    }
}
