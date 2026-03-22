package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class DIDependencyInjectionTest {

    class TestService(val name: String = "TestService")
    class TestRepository(val service: TestService)
    class TestController(val repository: TestRepository, val service: TestService)

    @Test
    fun `test singleton with one dependency`() {
        val container = di {
            singleton<TestService> { TestService("dependency") }
            singleton<TestRepository, TestService> { service: TestService -> TestRepository(service) }
        }

        val repository = container.get<TestRepository>()
        assertEquals("dependency", repository.service.name)
    }

    @Test
    fun `test singleton with two dependencies`() {
        val container = di {
            singleton<TestService> { TestService("service") }
            singleton<TestRepository, TestService> { service: TestService -> TestRepository(service) }
            singleton { repository: TestRepository, service: TestService ->
                TestController(repository, service)
            }
        }

        val controller = container.get<TestController>()
        assertEquals("service", controller.service.name)
        assertEquals("service", controller.repository.service.name)
        assertSame(controller.service, controller.repository.service)
    }

    @Test
    fun `test dependency injection with custom names`() {
        val container = di {
            singleton<TestService>("primaryService") { TestService("primary") }
            singleton<TestService>("secondaryService") { TestService("secondary") }
            singleton(
                dependencyName = "primaryService"
            ) { service: TestService -> TestRepository(service) }
        }

        val repository = container.get<TestRepository>()
        val primaryService = container.get<TestService>("primaryService")
        val secondaryService = container.get<TestService>("secondaryService")

        assertEquals("primary", repository.service.name)
        assertEquals("primary", primaryService.name)
        assertEquals("secondary", secondaryService.name)
        assertSame(primaryService, repository.service)
    }

    @Test
    fun `test complex dependency graph`() {
        val container = di {
            singleton<TestService>("service1") { TestService("service1") }
            singleton<TestService>("service2") { TestService("service2") }
            singleton("repo1", "service1") { service: TestService ->
                TestRepository(service)
            }
            singleton("repo2", "service2") { service: TestService ->
                TestRepository(service)
            }
        }

        val repo1 = container.get<TestRepository>("repo1")
        val repo2 = container.get<TestRepository>("repo2")

        assertEquals("service1", repo1.service.name)
        assertEquals("service2", repo2.service.name)
        assertNotSame(repo1.service, repo2.service)
    }
}
