package dev.tanvd.kcli.command

import dev.tanvd.kcli.command.args.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestCommand : CommandWithProperties() {
    val requiredArg by namedProperty("required", description = "Required argument") { required() }
    val optionalArg by namedProperty("optional", description = "Optional argument") { optional() }
    val posArg by property("Positional argument", positional(0).required())
    val boolFlag by namedProperty("bool-flag", description = "Boolean flag") { optional().boolean().default(false) }
    val explicitBoolFlag by namedProperty("explicit-bool", description = "Explicit boolean flag") { optional().boolean().default(false) }

    var executed = false
    var requiredArgState: String? = null
    var optionalArgState: String? = null
    var posArgState: String? = null
    var boolFlagState: Boolean? = null
    var explicitBoolFlagState: Boolean? = null

    override suspend fun execute() {
        executed = true
        requiredArgState = requiredArg
        optionalArgState = optionalArg
        posArgState = posArg
        boolFlagState = boolFlag
        explicitBoolFlagState = explicitBoolFlag
    }

    override val description: String = "Test command"
}

class ArgumentCommandTest {
    @Test
    fun `test required string argument`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("first --required value"))
        assertEquals("value", command.requiredArgState)
        assert(command.executed)
    }

    @Test
    fun `test missing required argument throws`() = runTest {
        val command = TestCommand()
        assertThrows<IllegalStateException> {
            command.run(CommandArgs.parse("first"))
        }
    }

    @Test
    fun `test optional string argument present`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("first --required value --optional opt"))
        assertEquals("value", command.requiredArgState)
        assertEquals("opt", command.optionalArgState)
        assert(command.executed)
    }

    @Test
    fun `test optional string argument missing`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("first --required value"))
        assertEquals("value", command.requiredArgState)
        assertNull(command.optionalArgState)
        assert(command.executed)
    }

    @Test
    fun `test positional argument`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("positional --required value"))
        assertEquals("positional", command.posArgState)
        assertEquals("value", command.requiredArgState)
        assert(command.executed)
    }

    @Test
    fun `test missing positional argument throws`() = runTest {
        val command = TestCommand()
        assertThrows<IllegalStateException> {
            command.run(CommandArgs.parse("--required value"))
        }
    }

    @Test
    fun `test boolean flag style argument`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("first --required value --bool-flag"))
        assertEquals("value", command.requiredArgState)
        assertTrue(command.boolFlagState!!)
        assert(command.executed)
    }

    @Test
    fun `test boolean flag with explicit true value`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("test first --required value --explicit-bool=true"))
        assertEquals("value", command.requiredArgState)
        assertTrue(command.explicitBoolFlagState!!)
        assert(command.executed)
    }

    @Test
    fun `test boolean flag with explicit false value`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("test first --required value --explicit-bool=false"))
        assertEquals("value", command.requiredArgState)
        assertFalse(command.explicitBoolFlagState!!)
        assert(command.executed)
    }

    @Test
    fun `test boolean flag absent defaults to false`() = runTest {
        val command = TestCommand()
        command.run(CommandArgs.parse("test first --required value"))
        assertEquals("value", command.requiredArgState)
        assertFalse(command.boolFlagState!!)
        assertFalse(command.explicitBoolFlagState!!)
        assert(command.executed)
    }
}
