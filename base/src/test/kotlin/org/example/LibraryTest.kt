package org.example

import kotlin.test.Test
import kotlin.test.assertTrue

class LibraryTest {
    @Test
    fun someLibraryMethodReturnsTrue() {
        assertTrue(
            Library().someLibraryMethod(),
            "someLibraryMethod should return 'true'"
        )
    }
}
