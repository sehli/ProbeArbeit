package org.example.project

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GreetingTest {

    private val greeting = Greeting()

    @Test
    fun greet_returnsNonNullString() {
        val result = greeting.greet()
        assertNotNull(result)
    }

    @Test
    fun greet_startsWithHello() {
        val result = greeting.greet()
        assertTrue(result.startsWith("Hello, "))
    }

    @Test
    fun greet_endsWithExclamation() {
        val result = greeting.greet()
        assertTrue(result.endsWith("!"))
    }

    @Test
    fun greet_containsPlatformName() {
        val platformName = getPlatform().name
        val result = greeting.greet()
        assertTrue(result.contains(platformName))
    }

    @Test
    fun greet_matchesExpectedFormat() {
        val expected = "Hello, ${getPlatform().name}!"
        assertEquals(expected, greeting.greet())
    }

    @Test
    fun greet_isDeterministic() {
        assertEquals(greeting.greet(), greeting.greet())
    }
}
