package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.trial.serialization.TrialJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class ExampleDataDeserializationTest {

    private val examplePatientRecordJson = ExampleFunctions.resolveExamplePatientRecordJson(LUNG_01_EXAMPLE)
    private val exampleTreatmentMatchJson = ExampleFunctions.resolveExampleTreatmentMatchJson(LUNG_01_EXAMPLE)
    private val exampleTrialDatabaseDir = ExampleFunctions.resolveExampleTrialDatabaseDirectory()

    @Test
    fun `Should be able to deserialize example data`() {
        assertThat(PatientRecordJson.read(examplePatientRecordJson)).isNotNull()
        assertThat(TreatmentMatchJson.read(exampleTreatmentMatchJson)).isNotNull()
        assertThat(TrialJson.readFromDir(exampleTrialDatabaseDir)).isNotNull()
    }
}