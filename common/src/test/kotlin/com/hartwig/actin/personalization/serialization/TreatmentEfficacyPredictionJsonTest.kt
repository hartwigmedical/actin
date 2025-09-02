package com.hartwig.actin.personalization.serialization

import com.hartwig.actin.datamodel.algo.TreatmentEfficacyPrediction
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
                "No Treatment" to TreatmentEfficacyPrediction(
                    survivalProbs = listOf(0.91, 0.84, 0.27),
                    shapValues = mapOf(
                        "age" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = -0.02),
                        "bmi" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = -0.01)
                    )
                ),
                "Treatment 1" to TreatmentEfficacyPrediction(
                    survivalProbs = listOf(0.93, 0.87, 0.54),
                    shapValues = mapOf(
                        "age" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = 0.01),
                        "bmi" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = 0.02)
                    )
                ),
                "Treatment 2" to TreatmentEfficacyPrediction(
                    survivalProbs = listOf(0.96, 0.88, 0.75),
                    shapValues = mapOf(
                        "age" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = 0.03),
                        "bmi" to TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = -0.04)
                    )
                )
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