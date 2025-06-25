package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.trial.serialization.TrialJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExampleDataDeserializationTest {

    @Test
    fun `Should be able to deserialize example data`() {
        listOf(LUNG_01_EXAMPLE, LUNG_02_EXAMPLE, LUNG_03_EXAMPLE, LUNG_04_EXAMPLE).forEach {
            val recordJson = ExampleFunctions.resolveExamplePatientRecordJson(it)
            assertThat(PatientRecordJson.read(recordJson)).isNotNull()
        }
        
        assertThat(TreatmentMatchJson.read(ExampleFunctions.resolveExampleTreatmentMatchJson(LUNG_01_EXAMPLE))).isNotNull()
        assertThat(TrialJson.readFromDir(ExampleFunctions.resolveExampleTrialDatabaseDirectory())).isNotNull()
    }
}