package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertFailsWith

class DIErrorHandlingTest {

    class TestService(val name: String = "TestService")

    @Test
    fun `test error on duplicate registration`() {
        assertFailsWith<IllegalArgumentException> {
            di {
                singleton<TestService> { TestService("first") }
                singleton<TestService> { TestService("second") }
            }
        }
    }

    @Test
    fun `test error on missing dependency`() {
        val container = di { }

        assertFailsWith<IllegalStateException> {
            container.get<TestService>()
        }
    }

    @Test
    fun `test error on override non-existent service`() {
        val container = DIContainer()

        assertFailsWith<IllegalArgumentException> {
            container.override(DIKey.of<TestService>()) { TestService() }
        }
    }
}
