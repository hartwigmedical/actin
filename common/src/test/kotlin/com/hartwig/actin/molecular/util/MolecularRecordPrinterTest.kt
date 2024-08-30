package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createExhaustiveTestMolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createMinimalTestMolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory.createProperTestMolecularRecord
import com.hartwig.actin.molecular.util.MolecularRecordPrinter.Companion.printRecord
import org.junit.Test

class MolecularRecordPrinterTest {

    @Test
    fun `Should print molecular records without error`() {
        printRecord(createExhaustiveTestMolecularRecord())
        printRecord(createProperTestMolecularRecord())
        printRecord(createMinimalTestMolecularRecord())
    }
}