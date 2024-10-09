package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.trial.serialization.TrialJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TestDataDeserializationTest {

    private val testPatientRecordJson = LocalTestFunctions.resourceOnClasspath("test_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val testTreatmentMatchJson = LocalTestFunctions.resourceOnClasspath("test_treatment_match/EXAMPLE-LUNG-01.treatment_match.json")
    private val testTrialRecordDatabaseDir = LocalTestFunctions.resourceOnClasspath("test_trial_database")

    @Test
    fun `Should be able to deserialize test data`() {
        assertThat(PatientRecordJson.read(testPatientRecordJson)).isNotNull
        assertThat(TreatmentMatchJson.read(testTreatmentMatchJson)).isNotNull
        assertThat(TrialJson.readFromDir(testTrialRecordDatabaseDir)).isNotNull
    }
}