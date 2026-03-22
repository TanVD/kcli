package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DIContainerUtilsTest {

    class TestService(val name: String = "TestService")
    class TestRepository(val service: TestService)

    @Test
    fun `test container has method`() {
        val container = di {
            singleton<TestService> { TestService() }
        }

        assertTrue(container.has(DIKey.of<TestService>()))
        assertFalse(container.has(DIKey.of<TestRepository>()))
    }

    @Test
    fun `test extension function get with type inference`() {
        val container = di {
            singleton<TestService> { TestService("extension") }
        }

        val service: TestService = container.get()
        assertEquals("extension", service.name)
    }

    @Test
    fun `test extension function get with custom name`() {
        val container = di {
            singleton<TestService>("namedService") { TestService("named") }
        }

        val service: TestService = container.get("namedService")
        assertEquals("named", service.name)
    }

    @Test
    fun `test di DSL function`() {
        val container = di {
            singleton<TestService> { TestService("dsl") }
        }

        assertEquals("dsl", container.get<TestService>().name)
        assertTrue(container.has(DIKey.of<TestService>()))
    }
}
