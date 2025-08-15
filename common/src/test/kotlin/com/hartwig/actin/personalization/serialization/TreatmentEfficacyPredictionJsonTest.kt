package com.hartwig.actin.personalization.serialization

import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class TreatmentEfficacyPredictionJsonTest {

    private val treatmentEfficacyPredictionJson =
        ResourceLocator.resourceOnClasspath("personalization" + File.separator + "treatment_efficacy_prediction.json")

    @Test
    fun `Should be able to read a treatment efficacy prediction json`() {
        val predictions = TreatmentEfficacyPredictionJson.read(treatmentEfficacyPredictionJson)

        assertThat(predictions).isEqualTo(
            mapOf(
                "No Treatment" to listOf(0.91, 0.84, 0.27),
                "Treatment 1" to listOf(0.93, 0.87, 0.54),
                "Treatment 2" to listOf(0.96, 0.88, 0.75),
            )
        )
    }
    
    @Test
    fun `Should return empty map when file is empty`() {
        val emptyFile = ResourceLocator.resourceOnClasspath("personalization" + File.separator + "empty.json")
        val predictions = TreatmentEfficacyPredictionJson.read(emptyFile)
        assertThat(predictions).isEmpty()
    }
}