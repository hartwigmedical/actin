package com.hartwig.actin.clinical.util

import com.hartwig.actin.TestPatientFactory.createMinimalTestPatientRecord
import com.hartwig.actin.TestPatientFactory.createProperTestPatientRecord
import com.hartwig.actin.clinical.util.PatientRecordPrinter.Companion.printRecord
import org.junit.Test

class PatientRecordPrinterTest {

    @Test
    fun `Should print clinical records`() {
        printRecord(createProperTestPatientRecord())
        printRecord(createMinimalTestPatientRecord())
    }
}