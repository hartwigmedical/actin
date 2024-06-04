package com.hartwig.actin

import com.hartwig.actin.molecular.datamodel.hmf.PatientRecordJson.fromJson
import com.hartwig.actin.molecular.datamodel.hmf.PatientRecordJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientRecordJsonTest {

    @Test
    fun `Should be able to read and write Patient Record to JSON`() {
        val patientRecord = TestPatientFactory.createExhaustiveTestPatientRecord()
        val convertedPatientRecord = fromJson(toJson(patientRecord))
        assertThat(convertedPatientRecord).isEqualTo(patientRecord)
    }
}