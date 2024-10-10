package com.hartwig.actin.system

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.trial.serialization.TrialJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExampleDataDeserializationTest {

    private val examplePatientRecordJson =
        LocalExampleFunctions.resourceOnClasspath("example_patient_data/EXAMPLE-LUNG-01.patient_record.json")
    private val exampleTreatmentMatchJson =
        LocalExampleFunctions.resourceOnClasspath("example_treatment_match/EXAMPLE-LUNG-01.treatment_match.json")
    private val exampleTrialDatabaseDir = LocalExampleFunctions.resourceOnClasspath("example_trial_database")

    @Test
    fun `Should be able to deserialize example data`() {
        assertThat(PatientRecordJson.read(examplePatientRecordJson)).isNotNull
        assertThat(TreatmentMatchJson.read(exampleTreatmentMatchJson)).isNotNull
        assertThat(TrialJson.readFromDir(exampleTrialDatabaseDir)).isNotNull
    }
}