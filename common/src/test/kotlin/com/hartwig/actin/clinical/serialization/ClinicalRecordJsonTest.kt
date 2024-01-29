package com.hartwig.actin.clinical.serialization

import com.google.common.io.Resources
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createMinimalTestClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory.createProperTestClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.fromJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.read
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.readFromDir
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class ClinicalRecordJsonTest {

    @Test
    fun canConvertBackAndForthJson() {
        val minimal = createMinimalTestClinicalRecord()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)
        val proper = createProperTestClinicalRecord()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }

    @Test
    fun canReadClinicalRecordDirectory() {
        val records = readFromDir(CLINICAL_DIRECTORY)
        assertThat(records).hasSize(1)
        assertClinicalRecord(records[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotReadFilesFromNonDir() {
        readFromDir(CLINICAL_JSON)
    }

    @Test
    fun canReadClinicalRecordJson() {
        assertClinicalRecord(read(CLINICAL_JSON))
    }

    companion object {
        private val CLINICAL_DIRECTORY = Resources.getResource("clinical" + File.separator + "records").path
        private val CLINICAL_JSON = CLINICAL_DIRECTORY + File.separator + "patient.clinical.json"
        private fun assertClinicalRecord(record: ClinicalRecord) {
            assertThat(record.patientId).isEqualTo("ACTN01029999")
            assertThat(record.priorSecondPrimaries).hasSize(1)
            assertThat(record.priorOtherConditions).hasSize(1)
            assertThat(record.complications!!).hasSize(1)
            assertThat(record.labValues).hasSize(2)
            assertThat(record.toxicities).hasSize(2)
            assertThat(record.intolerances).hasSize(2)
            assertThat(record.surgeries).hasSize(1)
            assertThat(record.vitalFunctions).hasSize(1)
            assertThat(record.bloodTransfusions).hasSize(1)
            assertThat(record.medications).hasSize(2)
        }
    }
}