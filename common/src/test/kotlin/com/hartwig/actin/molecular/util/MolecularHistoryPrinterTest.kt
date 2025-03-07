package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.junit.Test

class MolecularHistoryPrinterTest {

    @Test
    fun `Should print molecular history without errors`() {
        MolecularHistoryPrinter.print(TestMolecularFactory.createExhaustiveTestMolecularHistory())
        MolecularHistoryPrinter.print(TestMolecularFactory.createProperTestMolecularHistory())
        MolecularHistoryPrinter.print(TestMolecularFactory.createMinimalTestMolecularHistory())
    }
}