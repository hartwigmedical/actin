package com.hartwig.actin.molecular.util

import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createExhaustiveTestMolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createMinimalTestMolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createProperTestMolecularRecord
import com.hartwig.actin.molecular.util.MolecularPrinter.Companion.printRecord
import org.junit.Test

class MolecularPrinterTest {
    @Test
    fun canPrintMolecularRecords() {
        printRecord(createExhaustiveTestMolecularRecord())
        printRecord(createProperTestMolecularRecord())
        printRecord(createMinimalTestMolecularRecord())
    }
}