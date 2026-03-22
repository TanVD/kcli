package dev.tanvd.kcli.di

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class DIKeyTest {

    class TestService(val name: String = "TestService")

    @Test
    fun `test DIKey creation with default name`() {
        val key = DIKey.of<TestService>()
        assertEquals("TestService", key.name)
    }

    @Test
    fun `test DIKey creation with custom name`() {
        val key = DIKey.of<TestService>("CustomService")
        assertEquals("CustomService", key.name)
    }

    @Test
    fun `test DIKey equality`() {
        val key1 = DIKey.of<TestService>("test")
        val key2 = DIKey.of<TestService>("test")
        val key3 = DIKey.of<TestService>("different")

        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())
        assertNotSame(key1, key3)
    }
}
