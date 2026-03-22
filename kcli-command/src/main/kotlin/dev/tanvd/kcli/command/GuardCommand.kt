package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.ViewContext

/**
 * A proxy command that wraps another command and provides before/after call handlers.
 * This allows for intercepting and modifying command execution flow.
 */
class GuardCommand(
    private val delegate: Command,
    private val guard: suspend ((CommandArgs) -> Boolean),
) : Command {
    override suspend fun check(args: CommandArgs): Boolean = delegate.check(args)

    override suspend fun run(args: CommandArgs) {
        if (!this.guard(args)) return
        delegate.run(args)
    }

    override suspend fun describe(context: ViewContext) {
        delegate.describe(context)
    }
}
