package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.util.DatamodelPrinter
import org.junit.Test

class MolecularTestPrinterTest {

    private val printer = MolecularTestPrinter(DatamodelPrinter.withDefaultIndentation())

    @Test
    fun `Should print molecular tests without error`() {
        val tests = listOf(
            TestMolecularFactory.createExhaustiveWholeGenomeTest(),
            TestMolecularFactory.createProperWholeGenomeTest(),
            TestMolecularFactory.createMinimalWholeGenomeTest(),
            TestMolecularFactory.createExhaustivePanelTest(),
            TestMolecularFactory.createProperPanelTest(),
            TestMolecularFactory.createMinimalPanelTest()
        )

        printer.print(tests)
    }
}