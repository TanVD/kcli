package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals

class DIValueTest {

    class TestService(val name: String = "TestService")
    class TestRepository(val service: TestService)

    @Test
    fun `test DIValue creation and execution`() {
        val key = DIKey.of<TestService>()
        val diValue = DIValue(key, emptyList()) { TestService("value") }

        val container = DIContainer()
        val result = diValue.value(container)

        assertEquals("value", result.name)
    }

    @Test
    fun `test DIValue with dependencies`() {
        val serviceKey = DIKey.of<TestService>()
        val repositoryKey = DIKey.of<TestRepository>()

        val container = DIContainer()
        container.value(serviceKey) { TestService("dependency") }

        val diValue = DIValue(repositoryKey, listOf(serviceKey)) { deps ->
            TestRepository(deps[serviceKey] as TestService)
        }

        val result = diValue.value(container)
        assertEquals("dependency", result.service.name)
    }
}
