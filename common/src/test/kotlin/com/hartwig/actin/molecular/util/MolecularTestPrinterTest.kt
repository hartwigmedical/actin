package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.junit.Test

class MolecularTestPrinterTest {

    @Test
    fun `Should print ORANGE records without error`() {
        MolecularTestPrinter.printOrangeRecord(TestMolecularFactory.createExhaustiveTestMolecularRecord())
        MolecularTestPrinter.printOrangeRecord(TestMolecularFactory.createProperTestMolecularRecord())
        MolecularTestPrinter.printOrangeRecord(TestMolecularFactory.createMinimalTestMolecularRecord())
    }

    @Test
    fun `Should print panel records without error`() {
        MolecularTestPrinter.printPanelRecord(TestMolecularFactory.createExhaustiveTestPanelRecord())
        MolecularTestPrinter.printPanelRecord(TestMolecularFactory.createProperTestPanelRecord())
        MolecularTestPrinter.printPanelRecord(TestMolecularFactory.createMinimalTestPanelRecord())
    }
}