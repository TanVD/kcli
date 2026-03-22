package dev.tanvd.kcli.command.args

class CommandArgs(val positional: Map<Int, String>, val named: Map<String, String>) {
    fun isQualified(start: String): Boolean {
        return positional[0] == start
    }

    fun dequalify(start: String): CommandArgs {
        require(isQualified(start)) { "Command is not qualified" }
        val toSubtract = 1
        val positional = positional.entries.mapNotNull { (index, value) -> if (index < toSubtract) null else index - toSubtract to value }.toMap()
        return CommandArgs(positional, named)
    }

    fun drop(name: String) = CommandArgs(positional, named - name)

    companion object {
        fun parse(line: String): CommandArgs {
            val tokens = line.tokens()
            return parse(tokens)
        }

        fun parse(tokens: List<String>): CommandArgs {
            val positional = mutableMapOf<Int, String>()
            val named = mutableMapOf<String, String>()

            var positionalIndex = 0

            var skipNext = false
            for (index in tokens.indices) {
                if (skipNext) {
                    skipNext = false
                    continue
                }
                val token = tokens[index]
                if (token.startsWith("--")) {
                    val keyValue = token.removePrefix("--").split("=", limit = 2)
                    if (keyValue.size == 2) {
                        named[keyValue[0]] = keyValue[1]
                    } else if (index + 1 < tokens.size && !tokens[index + 1].startsWith("--")) {
                        named[keyValue[0]] = tokens[index + 1]
                        skipNext = true
                    } else {
                        named[keyValue[0]] = ""
                    }
                } else {
                    positional[positionalIndex] = token
                    positionalIndex++
                }
            }

            return CommandArgs(positional, named)
        }

        private fun String.tokens(): List<String> {
            return buildList {
                val token = StringBuilder()
                var insideDoubleQuotes = false
                var insideSingleQuotes = false

                for (char in this@tokens) {
                    when {
                        char == '"' && !insideSingleQuotes -> {
                            insideDoubleQuotes = !insideDoubleQuotes
                            if (!insideDoubleQuotes) add(token.toString().trim()).also { token.clear() }
                        }

                        char == '\'' && !insideDoubleQuotes -> {
                            insideSingleQuotes = !insideSingleQuotes
                            if (!insideSingleQuotes) add(token.toString().trim()).also { token.clear() }
                        }

                        char.isWhitespace() && !insideDoubleQuotes && !insideSingleQuotes -> {
                            if (token.isNotEmpty()) add(token.toString().trim()).also { token.clear() }
                        }

                        else -> token.append(char)
                    }
                }
                if (token.isNotEmpty()) add(token.toString().trim())
            }
        }
    }
}
