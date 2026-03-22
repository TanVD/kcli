# kcli-view

Terminal rendering system for Kotlin. Builds sequences of styled `ViewElement` values and renders them to stdout via
ANSI 24-bit RGB escape codes. No reflection — fully compatible with GraalVM native-image.

Package: `dev.tanvd.kcli.view`

## styled()

```kotlin
// import dev.tanvd.kcli.view.components.styled
fun ViewContext.styled(input: String): ViewContext
```

The primary way to write styled output. Parses inline markup in a single string and emits the corresponding
`ViewElement` values. Covers the common cases — bold, italic, color, emojis — without separate DSL calls.

### Syntax

#### Bold and italic

```
**bold text**
*italic text*
```

```kotlin
styled("**Error:** *check the logs*")
```

#### Styles and color — `[text]:[...]`

```
[text]:[style1,style2,color:NAME]
```

Style names match `TextStyle` enum values (case-insensitive): `bold`, `italic`, `faint`.
Color names match `Color` enum values (case-insensitive): `red`, `green`, `blue`, `yellow`, `magenta`, `cyan`, `black`,
`white`.

```kotlin
styled("[FAILED]:[bold,color:red]")
styled("[3 warnings]:[faint,color:yellow]")
styled("[info]:[italic,color:cyan]")
```

Multiple styles and a color can be combined in any order:

```kotlin
styled("[critical]:[bold,italic,color:red]")
```

#### Emojis — `:NAME:` and `{{NAME}}`

Emoji constants are looked up by name from the `Emojis` enum (case-insensitive). Two syntaxes are supported:

```
:CHECK_MARK:       Discord-style
{{CHECK_MARK}}     Brace syntax
```

```kotlin
styled(":check_mark: Done")
styled("{{rocket}} Deploying")
styled(":warning: Proceed with caution")
```

See the [Emojis](#emojis) section for available constants.

#### Escape sequences

Prefix any special character with `\` to emit it literally:

```kotlin
styled("\\*not italic\\*")          // → *not italic*
styled("\\[not a style block\\]")   // → [not a style block]
styled("\\:not an emoji\\:")        // → :not an emoji:
```

### Fallback behaviour

Unrecognized syntax is passed through as plain text:

- Unmatched `**` or `*` (no closing delimiter) → literal asterisks
- Unknown emoji name in `:...:` or `{{...}}` → literal text
- `[text]` without `:[...]` → literal text
- Unknown style or color names inside `[...]:[...]` → silently ignored; the rest of the styles still apply

### Full example

```kotlin
print {
    styled("{{rocket}} Deploying to [production]:[bold,color:red]")
    newline()
    styled("**Status:** :green_circle: running — [eta 30s]:[faint]")
    newline()
    styled(":warning: [1 warning]:[color:yellow]: missing health check")
}
```

---

## markdown()

```kotlin
// import dev.tanvd.kcli.view.components.markdown
fun ViewContext.markdown(content: String, color: Color? = null): ViewContext
```

Parses CommonMark using the JetBrains Markdown library and emits `ViewElement` values. Use this for multi-line,
pre-authored content where full Markdown syntax is more convenient than inline markup.

The optional `color` parameter applies a base color to all text elements in the output.

### Supported constructs

| Markdown                   | Rendered as                                           |
|----------------------------|-------------------------------------------------------|
| `# H1` through `###### H6` | `Text` with `BOLD` (hash prefix preserved in content) |
| `**text**`                 | `Text` with `BOLD`                                    |
| `*text*` or `_text_`       | `Text` with `ITALIC`                                  |
| `\n` (EOL token)           | `NewLine`                                             |
| Everything else            | Plain `Text` — syntax characters preserved            |

Inline code, fenced code blocks, links, and lists are passed through as-is. Their syntax characters (backticks,
brackets, `- `, `1.`) appear in the output unchanged.

### Example

```kotlin
print {
    markdown(
        """
        # Release notes

        Run **before** pushing. Use `--dry-run` to preview changes.

        - Step one
        - Step two with *emphasis*
    """.trimIndent()
    )
}
```

With base color:

```kotlin
print {
    markdown("**Warning:** configuration file not found.", color = Color.YELLOW)
}
```

---

## DSL Functions

All functions are extensions on `ViewContext` and return `this` for chaining.

### text

```kotlin
fun ViewContext.text(content: CharSequence, color: Color? = null, styles: Set<TextStyle> = emptySet()): ViewContext
fun ViewContext.text(content: CharSequence, builder: ViewElement.Text.Builder.() -> Unit): ViewContext
```

Emits one `Text` element per line in `content` (splits on `\n`), inserting `NewLine` elements between lines.

```kotlin
text("line one\nline two", Color.BLUE)
text("styled") { bold(); italic(); color(Color.YELLOW) }
```

### bold / italic / faint

```kotlin
fun ViewContext.bold(content: CharSequence, color: Color? = null): ViewContext
fun ViewContext.italic(content: CharSequence, color: Color? = null): ViewContext
fun ViewContext.faint(content: CharSequence, color: Color? = null): ViewContext
```

Convenience wrappers around `text()` that apply a single style.

```kotlin
bold("Error", Color.RED)
italic("(optional)")
faint("-- deprecated --")
```

### char

```kotlin
fun ViewContext.char(char: Char, color: Color? = null, styles: Set<TextStyle> = emptySet()): ViewContext
fun ViewContext.char(char: Char, builder: ViewElement.Text.Builder.() -> Unit): ViewContext
```

Emits a single character as a `Text` element.

### space / newline / br

```kotlin
fun ViewContext.space(): ViewContext        // emits ' '
fun ViewContext.newline(): ViewContext      // emits NewLine
fun ViewContext.br(): ViewContext           // emits two NewLine elements
```

### padding

```kotlin
suspend fun ViewContext.padding(
    prefix: CharSequence,
    withFirstLine: Boolean = true,
    content: suspend ViewContext.() -> Unit
): ViewContext
```

Runs `content` in a temporary `Container`, then re-emits every element with `prefix` prepended after each `NewLine`.
When `withFirstLine = true` (default), the prefix is also prepended before the first element.

```kotlin
padding(4.spaces) {
    text("indented line one")
    newline()
    text("indented line two")
}
// →  "    indented line one\n    indented line two"
```

`Int.spaces` is an extension property that produces a string of spaces:

```kotlin
padding(2.spaces) { ... }
```

### emoji

```kotlin
fun ViewContext.emoji(emojis: Emojis): ViewContext
```

Emits the emoji's symbol string as an unstyled `Text` element.

```kotlin
emoji(Emojis.CHECK_MARK)
space()
text("Done")
```

---

## Core Types

### ViewElement

```kotlin
sealed interface ViewElement {
    data class Text(
        val content: CharSequence,
        val color: Color? = null,
        val styles: Set<TextStyle> = emptySet()
    ) : ViewElement

    data object NewLine : ViewElement
}
```

`Text` carries a string with optional color and style set. `NewLine` represents a line break. All DSL functions produce
sequences of these two types.

`ViewElement.Text` has a builder for direct construction:

```kotlin
val element = ViewElement.Text.builder("hello")
    .bold()
    .italic()
    .color(Color.CYAN)
    .build()
```

### ViewContext

The receiver type for all DSL functions. Implementations collect or immediately emit elements.

```kotlin
interface ViewContext {
    fun element(element: ViewElement)
    fun elements(elements: List<ViewElement>)

    data class Container(val elements: MutableList<ViewElement>) : ViewContext
    object Printer : ViewContext   // writes directly to stdout
    class Text : ViewContext       // accumulates to a String; call .get()
}
```

**Container** — accumulates elements into a mutable list. Use it to build element sequences for inspection or deferred
rendering.

**Printer** — writes each element to stdout immediately via `TerminalScreen`. Used internally by `print {}`.

**Text** — serializes elements to a string in the format `[content]:[style1,style2,color]`. Intended for testing.

```kotlin
val rendered = ViewContext.Text().apply {
    bold("hello")
    text(" world")
}.get()
// "[hello]:[bold] world"
```

### Top-level functions

```kotlin
suspend fun print(content: suspend ViewContext.() -> Unit)
suspend fun container(content: suspend ViewContext.() -> Unit): ViewContext.Container
```

`print {}` writes to `ViewContext.Printer` and appends a trailing newline.

`container {}` returns a `ViewContext.Container` with all elements collected but not printed.

---

## Model

### Color

```kotlin
enum class Color(val id: String) {
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
}
```

Colors resolve to 24-bit RGB through a `Palette`. The active palette is `Color.Palette.current`, defaulting to `Light`.

| Color   | Light RGB     | Dark RGB      |
|---------|---------------|---------------|
| BLACK   | 40, 44, 52    | 171, 178, 191 |
| RED     | 224, 108, 117 | 224, 108, 117 |
| GREEN   | 80, 161, 79   | 152, 195, 121 |
| YELLOW  | 229, 192, 123 | 229, 192, 123 |
| BLUE    | 97, 175, 239  | 97, 175, 239  |
| MAGENTA | 198, 120, 221 | 198, 120, 221 |
| CYAN    | 86, 182, 194  | 86, 182, 194  |
| WHITE   | 220, 223, 228 | 40, 44, 52    |

### TextStyle

```kotlin
enum class TextStyle(val id: String) {
    BOLD("bold"),    // ANSI SGR 1
    FAINT("faint"),  // ANSI SGR 2
    ITALIC("italic") // ANSI SGR 3
}
```

### Emojis

108 named constants in `dev.tanvd.kcli.view.model.Emojis`. Each has `symbol: String` and `description: String`.

| Constant           | Symbol | Description            |
|--------------------|--------|------------------------|
| `CHECK_MARK`       | ✅      | Success/completion     |
| `CROSS_MARK`       | ❌      | Error/failure          |
| `WARNING`          | ⚠️     | Warning messages       |
| `GREEN_CIRCLE`     | 🟢     | Online/success status  |
| `RED_CIRCLE`       | 🔴     | Offline/error status   |
| `YELLOW_CIRCLE`    | 🟡     | Warning/pending status |
| `ROCKET`           | 🚀     | Deployments/launches   |
| `GEAR`             | ⚙️     | Settings/configuration |
| `SPARKLES`         | ✨      | New features           |
| `HOURGLASS`        | ⏳      | Waiting/loading        |
| `BUG`              | 🐛     | Debugging              |
| `ROBOT`            | 🤖     | AI/bot messages        |
| `LOCK`             | 🔒     | Auth/security          |
| `MAGNIFYING_GLASS` | 🔎     | Search                 |

Use `Emojis.entries` to iterate all constants.

---

## ANSI Output

`TerminalScreen` writes elements to `System.out`. `TerminalEscapeSequences.styled()` builds the escape sequence for a
`Text` element:

- Foreground color: `\e[38;2;R;G;Bm` (24-bit RGB from the active palette)
- Bold: `\e[1m`, Faint: `\e[2m`, Italic: `\e[3m`
- Reset: `\e[0m` after each styled segment

All codes are combined into a single escape sequence per `Text` element. Plain text (no color, no styles) is emitted
without any escape codes.
