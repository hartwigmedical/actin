package com.hartwig.actin.clinical.util

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createMinimalTestClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.util.ClinicalPrinter.Companion.printRecord
import org.junit.Test

class ClinicalPrinterTest {
    @Test
    fun canPrintClinicalRecords() {
        printRecord(createProperTestClinicalRecord())
        printRecord(createMinimalTestClinicalRecord())
    }
}