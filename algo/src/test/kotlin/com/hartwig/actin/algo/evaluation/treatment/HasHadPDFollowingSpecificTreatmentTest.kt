package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadPDFollowingSpecificTreatmentTest {

    @Test
    fun canEvaluate() {
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category but no correct name
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("other").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and name but missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("treatment 1").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category, name and stop reason PD
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .name("treatment 1")
                .stopReason(PD_LABEL)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatments: List<PriorTumorTreatment> = listOf(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .name("treatment 1")
                .bestResponse(PD_LABEL)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateStopReasons() {
        val treatment = "right treatment"
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()

        // Right category but different stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason("toxicity").bestResponse("improved").build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category but no stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason(null).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and right stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        // Add trial
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build()
                )
            )
        )
    }

    companion object {
        val FUNCTION =
            HasHadPDFollowingSpecificTreatment(listOf(TreatmentTestFactory.drugTherapy("treatment 1", TreatmentCategory.CHEMOTHERAPY)))
    }
}