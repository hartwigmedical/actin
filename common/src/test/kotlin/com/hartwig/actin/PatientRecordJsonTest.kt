package com.hartwig.actin

import com.hartwig.actin.PatientRecordJson.fromJson
import com.hartwig.actin.PatientRecordJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientRecordJsonTest {

    @Test
    fun `Should be able to read and write Patient Record to JSON`() {
        val patientRecord = TestPatientFactory.createProperTestPatientRecord()
        val convertedPatientRecord = fromJson(toJson(patientRecord))
        assertThat(convertedPatientRecord).isEqualTo(patientRecord)
    }
}