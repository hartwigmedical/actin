package com.hartwig.actin.algo.util

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import org.junit.Test

class TreatmentMatchPrinterTest {

    @Test
    fun `Should be able to print test treatment matches`() {
        TreatmentMatchPrinter.printMatch(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
        TreatmentMatchPrinter.printMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
    }
}