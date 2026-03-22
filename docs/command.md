# kcli-command

CLI argument parsing and command routing. Depends on `kcli-view`.

Package: `dev.tanvd.kcli.command`

## Command Interface

```kotlin
interface Command {
    suspend fun check(args: CommandArgs): Boolean
    suspend fun run(args: CommandArgs)
    suspend fun describe(context: ViewContext)
}
```

All three methods are suspend functions.

- `check` — returns `true` if this command can handle the given args. Used by routing commands to find a match.
- `run` — executes the command.
- `describe` — writes a human-readable description into a `ViewContext`. Used by `HelpCommand`.

## CommandArgs

```kotlin
class CommandArgs(val positional: Map<Int, String>, val named: Map<String, String>)
```

Holds parsed arguments split into two maps:

- `positional`: zero-based index to token, for all tokens that are not `--name` flags
- `named`: flag name to value, for all `--name value` and `--name=value` tokens

### Parsing

```kotlin
CommandArgs.parse(line: String): CommandArgs
CommandArgs.parse(tokens: List<String>): CommandArgs
```

The string parser tokenizes on whitespace, respecting `"double quoted"` and `'single quoted'` spans (quotes are
consumed, content is added as a single token).

Named argument forms:

| Input                                  | Result                   |
|----------------------------------------|--------------------------|
| `--key value`                          | `named["key"] = "value"` |
| `--key=value`                          | `named["key"] = "value"` |
| `--flag` (no following non-flag token) | `named["flag"] = ""`     |

Boolean flags use the empty-string convention: `--flag` stores `""`, which `.boolean()` converts to `true`.

```kotlin
val args = CommandArgs.parse("deploy --env production --dry-run")
// positional = {0: "deploy"}
// named = {"env": "production", "dry-run": ""}
```

### Utility Methods

```kotlin
fun isQualified(start: String): Boolean   // positional[0] == start
fun dequalify(start: String): CommandArgs // drops positional[0], re-indexes remaining
fun drop(name: String): CommandArgs       // returns copy without named[name]
```

`dequalify` is used by `NamedCommand` to strip its indicator token before passing args to the delegate:

```kotlin
// input:  positional={0:"deploy", 1:"myapp"}, named={"env":"prod"}
// after dequalify("deploy"):
//         positional={0:"myapp"}, named={"env":"prod"}
```

## CommandWithProperties

Abstract base class providing property delegation for typed argument reading.

```kotlin
abstract class CommandWithProperties : Command {
    abstract suspend fun execute()
    abstract val description: String

    fun <T> property(description: String, getter: CommandArgsReader<CommandArgs, T>): CommandProperty<T>
    fun <T> namedProperty(name: String, description: String, build: ...): CommandProperty<T>
    fun <T> namedOrPositionalProperty(name: String, index: Int = 0, description: String, build: ...): CommandProperty<T>
}
```

Override `execute()` instead of `run()`. The `run()` implementation fills all properties from args, calls `execute()`,
then resets them. A `Mutex` serializes concurrent calls.

```kotlin
class BuildCommand : CommandWithProperties() {
    val target by namedProperty("target", "Build target") { required() }
    val jobs by namedProperty("jobs", "Parallel job count") { optional().int().default(4) }
    val verbose by namedProperty("verbose", "Enable verbose output") {
        optional().boolean().default(false)
    }

    override val description = "Build the project"

    override suspend fun execute() {
        println("Building $target with $jobs jobs (verbose=$verbose)")
    }

    override suspend fun describe(context: ViewContext) = super.describe(context)
}
```

`describe()` from `CommandWithProperties` renders each `namedProperty`/`namedOrPositionalProperty` as
`--name: description`, followed by the command's `description` string. Properties registered via `property()` directly
appear in the description text only if they supply a non-empty description string.

### namedProperty

```kotlin
fun <T> namedProperty(
    name: String,
    description: String,
    build: CommandArgsReader<CommandArgs, String?>.() -> CommandArgsReader<CommandArgs, T>
): CommandProperty<T>
```

Reads `--name` from args. The `build` lambda chains reader transformations:

```kotlin
val port by namedProperty("port", "Server port") { required().int() }
val host by namedProperty("host", "Server host") { optional().default("localhost") }
```

### namedOrPositionalProperty

```kotlin
fun <T> namedOrPositionalProperty(
    name: String,
    index: Int = 0,
    description: String,
    build: CommandArgsReader<CommandArgs, String?>.() -> CommandArgsReader<CommandArgs, T>
): CommandProperty<T>
```

Reads `--name` if present, otherwise falls back to `positional[index]`.

```kotlin
val path by namedOrPositionalProperty("path", index = 0, "Target path") { required() }
// matches: "command /tmp/foo" OR "command --path /tmp/foo"
```

### property

```kotlin
fun <T> property(description: String, getter: CommandArgsReader<CommandArgs, T>): CommandProperty<T>
```

Low-level registration for arbitrary readers. Use reader factory functions directly:

```kotlin
val first by property("First positional arg", positional(0).required())
val second by property("Second positional arg", positional(1).optional())
```

## CommandProperty

```kotlin
class CommandProperty<T>(
    val getter: (CommandArgs) -> T,
    val description: String
) : ReadOnlyProperty<Command, T>
```

Implements `ReadOnlyProperty` for Kotlin property delegation. Value is `null` until `fillFrom(args)` is called;
accessing it before `run()` dispatches returns an unchecked null cast.

```kotlin
// Internal lifecycle managed by CommandWithProperties.run():
prop.fillFrom(args)   // populate
execute()             // read via delegation
prop.reset()          // clear back to null
```

## Reader Functions

`CommandArgsReader<Input, Output>` is a type alias for `(Input) -> Output`.

### Source Readers

```kotlin
fun named(name: String): CommandArgsReader<CommandArgs, String?>
fun positional(index: Int): CommandArgsReader<CommandArgs, String?>
fun single(): CommandArgsReader<CommandArgs, String?>              // positional(0)
fun namedOrPositional(name: String, index: Int = 0): CommandArgsReader<CommandArgs, String?>
```

### Cardinality

```kotlin
fun <I, O> CommandArgsReader<I, O?>.required(): CommandArgsReader<I, O>   // throws if null
fun <I, O> CommandArgsReader<I, O?>.optional(): CommandArgsReader<I, O?>  // identity, documents intent
fun <I, O> CommandArgsReader<I, O?>.default(value: O): CommandArgsReader<I, O>
```

`.required()` throws `IllegalStateException` with "Argument is required" when the value is null at runtime.

### Type Conversions

All converters have two overloads: one for nullable input (returns nullable output) and one for non-nullable input (
returns non-nullable output).

```kotlin
// String? -> String?  or  String -> String
fun CommandArgsReader<*, String?>.string(): ...
fun CommandArgsReader<String, String>.string(): ...

// -> Int / Int?
fun CommandArgsReader<*, String?>.int(): CommandArgsReader<*, Int?>
fun CommandArgsReader<*, String>.int(): CommandArgsReader<*, Int>

// -> Boolean / Boolean?
// blank string (from bare --flag) converts to true
fun CommandArgsReader<*, String?>.boolean(): CommandArgsReader<*, Boolean?>
fun CommandArgsReader<*, String>.boolean(): CommandArgsReader<*, Boolean>

// -> Long / Long?
fun CommandArgsReader<*, String?>.long(): ...
fun CommandArgsReader<*, String>.long(): ...

// -> Double / Double?
fun CommandArgsReader<*, String?>.double(): ...
fun CommandArgsReader<*, String>.double(): ...
```

### list

```kotlin
fun <I, O> CommandArgsReader<I, String?>.list(
    separator: String = ";",
    transform: (String) -> O
): CommandArgsReader<I, List<O>>
```

Splits on `separator`, trims each element, applies `transform`. Returns an empty list if the argument is absent or
blank.

```kotlin
val tags by namedProperty("tags", "Comma-separated tags") {
    list(",") { it.trim() }
}
// --tags "alpha,beta,gamma" -> listOf("alpha", "beta", "gamma")
```

### deserialize

```kotlin
fun <I, Oi, Or> CommandArgsReader<I, Oi>.deserialize(body: (Oi) -> Or): CommandArgsReader<I, Or>
```

General-purpose transformation step. Applies `body` to the current output.

```kotlin
val level by namedProperty("level", "Log level") {
    required().deserialize { LogLevel.valueOf(it.uppercase()) }
}
```

## Routing Commands

### NamedCommand

```kotlin
class NamedCommand(private val indicator: String, private val command: Command) : Command
```

Matches when `positional[0] == indicator`. Strips the indicator token (dequalifies) before delegating to `command`.

```kotlin
NamedCommand("build", BuildCommand())
// matches args starting with "build"
// delegates with "build" removed from positional
```

`describe()` renders the indicator in bold, then the delegate's description indented with two spaces.

### GroupCommand

```kotlin
class GroupCommand(
    private val commands: List<Command>,
    private val default: Command? = null
) : Command
```

Routes to the first command whose `check()` returns `true`. If no command matches, runs `default` if set, otherwise
throws.

```kotlin
val root = GroupCommand(
    NamedCommand("deploy", DeployCommand()),
    NamedCommand("rollback", RollbackCommand()),
    default = HelpCommand("Unknown command.", listOf(DeployCommand(), RollbackCommand()))
)
```

Help is triggered automatically when:

- `positional[0] == "help"`, or
- no positional arguments are present and `--help` flag is set (value `""` or `"true"`)

When help is triggered, a `HelpCommand` wrapping all registered commands is run instead.

### HelpCommand

```kotlin
class HelpCommand(private val description: String, private val commands: List<Command>) : Command
```

Prints `description`, a blank line, then calls `describe()` on each command.

`check()` returns `true` when `positional[0] == "help"` or no positionals and `--help` is present.

```kotlin
val help = HelpCommand("Available commands:", listOf(buildCmd, deployCmd))
```

### GuardCommand

```kotlin
class GuardCommand(
    private val delegate: Command,
    private val guard: suspend (CommandArgs) -> Boolean
) : Command
```

Runs `guard` before delegating. If `guard` returns `false`, execution stops silently (no error). `check()` delegates to
the wrapped command unchanged.

```kotlin
GuardCommand(AdminCommand()) { args ->
    currentUser.hasRole("admin").also {
        if (!it) print { text("Permission denied", Color.RED) }
    }
}
```

### ProxyCommand

```kotlin
class ProxyCommand(
    private val delegate: Command,
    private val beforeHandler: ((CommandArgs) -> CommandArgs)? = null,
    private val afterHandler: ((CommandArgs) -> Unit)? = null
) : Command
```

Wraps a delegate with optional before and after handlers. `beforeHandler` can transform the args before they reach the
delegate. `afterHandler` runs in a `finally` block, so it executes even if the delegate throws.

```kotlin
ProxyCommand(
    delegate = MyCommand(),
    beforeHandler = { args ->
        println("Running: ${args.positional[0]}")
        args
    },
    afterHandler = { _ ->
        println("Done")
    }
)
```

## Typical Wiring

```kotlin
suspend fun main(rawArgs: Array<String>) {
    val args = CommandArgs.parse(rawArgs.joinToString(" "))

    val root = GroupCommand(
        NamedCommand("build",  BuildCommand()),
        NamedCommand("deploy", DeployCommand()),
        NamedCommand("test",   TestCommand())
    )

    if (root.check(args)) {
        root.run(args)
    } else {
        print { text("No matching command. Run with 'help' for usage.") }
    }
}
```
