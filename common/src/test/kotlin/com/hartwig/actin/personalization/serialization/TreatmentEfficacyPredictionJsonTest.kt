package com.hartwig.actin.personalization.serialization

import com.hartwig.actin.datamodel.algo.PersonalizedTreatmentSummary
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
            PersonalizedTreatmentSummary(
                listOf(
                    PersonalizedTreatmentSummary.TreatmentEfficacyPrediction(
                        treatment = "No Treatment",
                        survivalProbs = listOf(0.91, 0.84, 0.27),
                        shapValues = mapOf(
                            "age" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = -0.02),
                            "bmi" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = -0.01)
                        )
                    ),
                    PersonalizedTreatmentSummary.TreatmentEfficacyPrediction(
                        treatment = "Treatment 1",
                        survivalProbs = listOf(0.93, 0.87, 0.54),
                        shapValues = mapOf(
                            "age" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = 0.01),
                            "bmi" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = 0.02)
                        )
                    ),
                    PersonalizedTreatmentSummary.TreatmentEfficacyPrediction(
                        treatment = "Treatment 2",
                        survivalProbs = listOf(0.96, 0.88, 0.75),
                        shapValues = mapOf(
                            "age" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 45.0, shapValue = 0.03),
                            "bmi" to PersonalizedTreatmentSummary.TreatmentEfficacyPrediction.ShapDetail(featureValue = 22.0, shapValue = -0.04)
                        )
                    )
                ),
                PersonalizedTreatmentSummary.SimilarPatientsSummary(
                    overallTreatmentProportion = listOf(
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "No Treatment", proportion = 0.5),
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "Treatment 1", proportion = 0.2),
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "Treatment 2", proportion = 0.3)
                    ),
                    similarPatientsTreatmentProportion = listOf(
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "No Treatment", proportion = 0.0),
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "Treatment 1", proportion = 0.4),
                        PersonalizedTreatmentSummary.SimilarPatientsSummary.TreatmentProportion(treatment = "Treatment 2", proportion = 0.6)
                    )
                )
            )
        )
    }
    
    @Test
    fun `Should return empty map when file is empty`() {
        val emptyFile = ResourceLocator.resourceOnClasspath("personalization" + File.separator + "empty.json")
        val summary = TreatmentEfficacyPredictionJson.read(emptyFile)
        assertThat(summary.predictions).isNull()
        assertThat(summary.similarPatientsSummary).isNull()
    }
}