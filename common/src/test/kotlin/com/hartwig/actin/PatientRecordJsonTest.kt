package com.hartwig.actin

import com.hartwig.actin.PatientRecordJson.fromJson
import com.hartwig.actin.PatientRecordJson.toJson
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientRecordJsonTest {

    @Test
    fun `Should be able to read and write Patient Record to JSON`() {
        val patientRecord = TestPatientFactory.createExhaustiveTestPatientRecord()
        val convertedPatientRecord = fromJson(toJson(patientRecord))
        assertThat(convertedPatientRecord).isEqualTo(patientRecord)
    }

    @Test
    fun `Should serialize special floating point values`() {
        val patientRecord = TestPatientFactory.createExhaustiveTestPatientRecord().copy(
            labValues = listOf(
                LabValue(
                    date = LocalDate.now(),
                    code = LabMeasurement.ASPARTATE_AMINOTRANSFERASE.code,
                    name = "Aspartate aminotransferase",
                    comparator = "",
                    value = Double.NaN,
                    unit = LabMeasurement.ASPARTATE_AMINOTRANSFERASE.defaultUnit,
                )
            )
        )
        val convertedPatientRecord = fromJson(toJson(patientRecord))
        assertThat(convertedPatientRecord).isEqualTo(patientRecord)
    }
}