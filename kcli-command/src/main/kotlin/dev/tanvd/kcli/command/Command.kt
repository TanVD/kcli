package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.ViewContext

interface Command {
    suspend fun check(args: CommandArgs): Boolean

    /**
     * Runs this command with the given arguments
     */
    suspend fun run(args: CommandArgs)

    /**
     * Outputs into view context the description of the command
     */
    suspend fun describe(context: ViewContext)
}
