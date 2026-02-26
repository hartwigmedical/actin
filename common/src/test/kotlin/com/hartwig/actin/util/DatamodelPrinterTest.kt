package com.hartwig.actin.util

import org.junit.jupiter.api.Test

class DatamodelPrinterTest {

    @Test
    fun `Should print with varying indents`() {
        for (i in 0..9) {
            val printer = DatamodelPrinter(i)
            printer.print("hi")
        }
    }
}