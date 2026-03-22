package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.CommandArgs
import dev.tanvd.kcli.view.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NamedCommand(private val indicator: String, private val command: Command) : Command {

    private val lock = Mutex()

    override suspend fun check(args: CommandArgs): Boolean = args.isQualified(indicator)

    override suspend fun run(args: CommandArgs) = lock.withLock {
        val updated = if (args.isQualified(indicator)) args.dequalify(indicator) else args
        command.run(updated)
    }

    override suspend fun describe(context: ViewContext) {
        context.apply {
            bold(indicator)
            newline()
            padding(2.spaces) {
                command.describe(this)
            }
        }
    }
}
