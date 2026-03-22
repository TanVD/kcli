# kcli

Kotlin CLI library for building native-friendly command-line tools. Covers terminal rendering, command parsing, and dependency injection — all three modules are designed to compile cleanly with GraalVM native-image with no reflection and no classpath scanning.

**GraalVM native-image compatibility was the primary reason this library exists.** Most CLI frameworks rely on reflection-based argument parsing, dynamic proxies, or runtime annotation processing — all of which require heavyweight GraalVM configuration. kcli avoids all of that: commands are plain classes, DI uses type-safe keys instead of reflection, and rendering is a pure function over a sealed type.

```
dev.tanvd.kcli — group = "dev.tanvd.kcli", version = "0.1.0"
```

## Modules

| Module         | Package                  | Purpose                                  |
|----------------|--------------------------|------------------------------------------|
| `kcli-view`    | `dev.tanvd.kcli.view`    | Terminal output with ANSI styling        |
| `kcli-command` | `dev.tanvd.kcli.command` | CLI argument parsing and command routing |
| `kcli-di`      | `dev.tanvd.kcli.di`      | Reflection-free dependency injection     |

**Dependencies between modules:** `kcli-command` depends on `kcli-view`. `kcli-di` is standalone.

## Quick Start

### kcli-view — styled output

The primary way to emit styled terminal output is `styled()` — a single-string inline markup function that handles bold, italic, colors, and emojis in one call:

```kotlin
import dev.tanvd.kcli.view.*
import dev.tanvd.kcli.view.components.styled

suspend fun main() {
    print {
        styled("{{rocket}} Deploying to [production]:[bold,color:red]")
        newline()
        styled("**Done** :check_mark: [elapsed: 3.2s]:[faint]")
    }
}
```

Supported syntax:

| Syntax                           | Result                        |
|----------------------------------|-------------------------------|
| `**text**`                       | bold                          |
| `*text*`                         | italic                        |
| `[text]:[bold,italic,color:red]` | arbitrary styles + color      |
| `:CHECK_MARK:`                   | emoji by name (Discord-style) |
| `{{CHECK_MARK}}`                 | emoji by name (brace syntax)  |
| `\*`, `\[`, `\:`                 | escaped literal               |

For long-form content, use `markdown()`:

```kotlin
import dev.tanvd.kcli.view.components.markdown

print {
    markdown("# Deploy\n\nRun **before** committing. Pass `--dry-run` to preview.")
}
```

For fine-grained control, use the DSL directly:

```kotlin
print {
    bold("Build complete", Color.GREEN)
    space()
    faint("(3 warnings)")
    br()
    emoji(Emojis.CHECK_MARK)
    text(" All checks passed")
}
```

### kcli-command

Define a command with typed property delegation:

```kotlin
import dev.tanvd.kcli.command.*
import dev.tanvd.kcli.command.args.*

class DeployCommand : CommandWithProperties() {
    val env by namedProperty("env", "Target environment") { required() }
    val dryRun by namedProperty("dry-run", "Skip actual deployment") {
        optional().boolean().default(false)
    }

    override val description = "Deploy to the target environment"

    override suspend fun execute() {
        print { styled("Deploying to **$env** (dry-run=$dryRun)") }
    }
}
```

Route commands with `GroupCommand` and `NamedCommand`:

```kotlin
val root = GroupCommand(
    NamedCommand("deploy", DeployCommand()),
    NamedCommand("rollback", RollbackCommand()),
)

val args = CommandArgs.parse(argv.joinToString(" "))
if (root.check(args)) root.run(args)
```

### kcli-di

Reflection-free singleton container — safe for GraalVM native-image without any additional configuration:

```kotlin
import dev.tanvd.kcli.di.*

val container = di {
    singleton<Database> { Database("jdbc:postgresql://localhost/app") }
    singleton<UserService, Database> { db -> UserService(db) }
}

val service = container.get<UserService>()
```

Override registrations for tests:

```kotlin
container.override(DIKey.of<Database>()) { Database("jdbc:h2:mem:test") }
```

## Module Docs

- [kcli-view reference](docs/view.md)
- [kcli-command reference](docs/command.md)
- [kcli-di reference](docs/di.md)
