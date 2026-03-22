package dev.tanvd.kcli.command.args

import dev.tanvd.kcli.command.Command
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

typealias CommandArgsReader<Input, Output> = (Input) -> Output

class CommandProperty<T>(val getter: (CommandArgs) -> T, val description: String) : ReadOnlyProperty<Command, T> {
    private var value: T? = null

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Command, property: KProperty<*>): T = value as T

    fun fillFrom(args: CommandArgs) {
        value = getter(args)
    }

    fun reset() {
        value = null
    }
}


fun named(name: String): CommandArgsReader<CommandArgs, String?> = { args -> args.named[name] }
fun positional(index: Int): CommandArgsReader<CommandArgs, String?> = { args -> args.positional[index] }
fun single(): CommandArgsReader<CommandArgs, String?> = positional(0)
fun namedOrPositional(name: String, index: Int = 0): CommandArgsReader<CommandArgs, String?> = { args ->
    args.named[name] ?: args.positional[index]
}

fun <Input, Output> CommandArgsReader<Input, Output?>.default(value: Output): CommandArgsReader<Input, Output> = { this(it) ?: value }

fun <Input, Output> CommandArgsReader<Input, Output?>.optional(): CommandArgsReader<Input, Output?> = this
fun <Input, Output> CommandArgsReader<Input, Output?>.required(): CommandArgsReader<Input, Output> = { this(it) ?: error("Argument is required") }

@JvmName("optionalString")
fun <Input> CommandArgsReader<Input, String?>.string(): CommandArgsReader<Input, String?> = this
fun CommandArgsReader<String, String>.string(): CommandArgsReader<String, String> = this

@JvmName("optionalInt")
fun <Input> CommandArgsReader<Input, String?>.int(): CommandArgsReader<Input, Int?> = { this(it)?.toInt() }
fun <Input> CommandArgsReader<Input, String>.int(): CommandArgsReader<Input, Int> = { this(it).toInt() }

fun <Input, Output> CommandArgsReader<Input, String?>.list(separator: String = ";", transform: (String) -> Output): CommandArgsReader<Input, List<Output>> = { input ->
    val argument = this(input)?.takeIf { it.isNotBlank() }
    argument?.split(separator)?.map { transform(it.trim()) } ?: emptyList()
}

@JvmName("optionalBoolean")
fun <Input> CommandArgsReader<Input, String?>.boolean(): CommandArgsReader<Input, Boolean?> = {
        val value = this(it)
        when {
            value == null -> null
            value.isBlank() -> true
            else -> value.lowercase().toBooleanStrict()
        }
    }

fun <Input> CommandArgsReader<Input, String>.boolean(): CommandArgsReader<Input, Boolean> =
    {
        val value = this(it)
        when {
            value.isBlank() -> true
            else -> value.toBooleanStrict()
        }
    }

@JvmName("optionalDouble")
fun <Input> CommandArgsReader<Input?, String?>.double(): CommandArgsReader<Input, Double?> = { this(it)?.toDouble() }
fun <Input> CommandArgsReader<Input, String>.double(): CommandArgsReader<Input, Double> = { this(it).toDouble() }

@JvmName("optionalLong")
fun <Input> CommandArgsReader<Input, String?>.long(): CommandArgsReader<Input, Long?> = { this(it)?.toLong() }
fun <Input> CommandArgsReader<Input, String>.long(): CommandArgsReader<Input, Long> = { this(it).toLong() }

fun <Input, OutputInitial, OutputResult> CommandArgsReader<Input, OutputInitial>.deserialize(body: (OutputInitial) -> OutputResult): CommandArgsReader<Input, OutputResult> =
    { this(it).let(body) }
