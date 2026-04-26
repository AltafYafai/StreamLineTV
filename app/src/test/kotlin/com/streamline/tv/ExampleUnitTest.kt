package com.streamline.tv

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun timeFormatting_isCorrect() {
        assertEquals("00:10", formatTime(10000))
        assertEquals("01:00", formatTime(60000))
        assertEquals("1:00:00", formatTime(3600000))
    }
}
