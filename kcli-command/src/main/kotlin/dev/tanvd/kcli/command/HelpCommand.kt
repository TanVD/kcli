package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.*

class HelpCommand(
    private val description: String,
    private val commands: List<Command>
) : Command {
    override suspend fun check(args: CommandArgs): Boolean {
        return args.isQualified("help") || args.positional.isEmpty() && args.named.containsKey("help")
    }

    override suspend fun run(args: CommandArgs) {
        print {
            text(description)
            br()
            commands.forEach { command ->
                command.describe(this)
            }
        }
    }

    override suspend fun describe(context: ViewContext) {
        context.apply {
            text("Describes available commands")
        }
    }
}
