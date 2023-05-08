package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadPDFollowingTreatmentWithCategoryTest {
    @Test
    fun canEvaluate() {
        val function = HasHadPDFollowingTreatmentWithCategory(TreatmentCategory.CHEMOTHERAPY)
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category but no PD
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .stopReason("toxicity")
                .bestResponse("improved")
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category, type and stop reason PD
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldPassForMatchingCategoryWhenPDIsIndicatedInBestResponse() {
        val function = HasHadPDFollowingTreatmentWithCategory(TreatmentCategory.CHEMOTHERAPY)
        val treatments = listOf<PriorTumorTreatment>(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .bestResponse(PD_LABEL)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrial() {
        val function = HasHadPDFollowingTreatmentWithCategory(TreatmentCategory.CHEMOTHERAPY)

        // Add one trial
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build()
                )
            )
        )
    }
}