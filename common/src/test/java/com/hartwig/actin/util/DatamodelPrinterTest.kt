package com.hartwig.actin.util

import org.junit.Test

class DatamodelPrinterTest {
    @Test
    fun canPrintWithVaryingIndents() {
        for (i in 0..9) {
            val printer = DatamodelPrinter(i)
            printer.print("hi")
        }
    }
}