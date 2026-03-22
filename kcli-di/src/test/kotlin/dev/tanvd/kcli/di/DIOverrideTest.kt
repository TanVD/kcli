package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class DIOverrideTest {

    class TestService(val name: String = "TestService")
    class TestRepository(val service: TestService)

    @Test
    fun `test override functionality`() {
        val container = di {
            singleton<TestService> { TestService("original") }
            override<TestService> { TestService("overridden") }
        }

        val service = container.get<TestService>()
        assertEquals("overridden", service.name)
    }

    @Test
    fun `test override invalidates dependent cache`() {
        val container = di {
            singleton<TestService> { TestService("original") }
            singleton { service: TestService -> TestRepository(service) }
        }

        val originalRepository = container.get<TestRepository>()
        assertEquals("original", originalRepository.service.name)

        container.override(DIKey.of<TestService>()) { TestService("overridden") }

        val newRepository = container.get<TestRepository>()
        assertEquals("overridden", newRepository.service.name)
        assertNotSame(originalRepository, newRepository)
    }
}
