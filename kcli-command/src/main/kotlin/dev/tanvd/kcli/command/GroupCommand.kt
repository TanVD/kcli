package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.ViewContext

class GroupCommand(
    private val commands: List<Command>,
    private val default: Command? = null
) : Command {
    private val help = HelpCommand("Following commands are available:", commands)

    constructor(vararg commands: Command, default: Command? = null) : this(commands.toList(), default)

    override suspend fun check(args: CommandArgs): Boolean {
        return commands.any { it.check(args) } || default != null
    }

    override suspend fun run(args: CommandArgs) {
        if (isHelp(args)) {
            help.run(args)
            return
        }
        val command = commands.find { it.check(args) }
        command?.run(args) ?: default?.run(args) ?: error("No matching command found")
    }

    override suspend fun describe(context: ViewContext) {
        context.apply {
            commands.forEach { command ->
                command.describe(this)
            }
        }
    }

    private fun isHelp(args: CommandArgs): Boolean {
        return args.isQualified("help") || (args.positional.isEmpty() && args.named["help"] == "" || args.named["help"] == "true")
    }
}
