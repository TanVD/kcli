package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DISingletonTest {

    class TestService(val name: String = "TestService")

    @Test
    fun `test singleton registration without dependencies`() {
        val container = di {
            singleton<TestService> { TestService("test") }
        }

        val service = container.get<TestService>()
        assertEquals("test", service.name)
    }

    @Test
    fun `test singleton registration with custom name`() {
        val container = di {
            singleton<TestService>("customService") { TestService("custom") }
        }

        val service = container.get<TestService>("customService")
        assertEquals("custom", service.name)
    }

    @Test
    fun `test singleton caching`() {
        val container = di {
            singleton<TestService> { TestService("cached") }
        }

        val service1 = container.get<TestService>()
        val service2 = container.get<TestService>()

        assertSame(service1, service2)
    }
}
