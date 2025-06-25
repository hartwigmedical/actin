package com.hartwig.actin.system.example

import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.trial.serialization.TrialJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExampleDataDeserializationTest {

    @Test
    fun `Should be able to deserialize example data`() {
        assertThat(PatientRecordJson.read(ExampleFunctions.resolveExamplePatientRecordJson(LUNG_01_EXAMPLE))).isNotNull()
        assertThat(PatientRecordJson.read(ExampleFunctions.resolveExamplePatientRecordJson(LUNG_02_EXAMPLE))).isNotNull()
        assertThat(PatientRecordJson.read(ExampleFunctions.resolveExamplePatientRecordJson(LUNG_03_EXAMPLE))).isNotNull()
        assertThat(PatientRecordJson.read(ExampleFunctions.resolveExamplePatientRecordJson(LUNG_04_EXAMPLE))).isNotNull()
        
        assertThat(TreatmentMatchJson.read(ExampleFunctions.resolveExampleTreatmentMatchJson(LUNG_01_EXAMPLE))).isNotNull()
        assertThat(TrialJson.readFromDir(ExampleFunctions.resolveExampleTrialDatabaseDirectory())).isNotNull()
    }
}