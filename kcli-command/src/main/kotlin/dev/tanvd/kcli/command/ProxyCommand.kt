package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.ViewContext

/**
 * A proxy command that wraps another command and provides before/after call handlers.
 * This allows for intercepting and modifying command execution flow.
 */
class ProxyCommand(
    private val delegate: Command,
    private val beforeHandler: ((CommandArgs) -> CommandArgs)? = null,
    private val afterHandler: ((CommandArgs) -> Unit)? = null
) : Command {
    override suspend fun check(args: CommandArgs): Boolean = delegate.check(args)

    override suspend fun run(args: CommandArgs) {
        val processedArgs = beforeHandler?.invoke(args) ?: args
        try {
            delegate.run(processedArgs)
        } finally {
            afterHandler?.invoke(processedArgs)
        }
    }

    override suspend fun describe(context: ViewContext) {
        delegate.describe(context)
    }
}
