package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import org.junit.Test

class HasHadPDFollowingSomeSystemicTreatmentsTest {
    @Test
    fun canEvaluate() {
        val function = HasHadPDFollowingSomeSystemicTreatments(1, false)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one systemic with stop reason PD
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2020).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add a later systemic with other stop reason
        treatments.add(
            TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2021).stopReason("toxicity")
                .bestResponse("improved").build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldPassIfLastSystemicTreatmentIndicatesPDInBestResponse() {
        val function = HasHadPDFollowingSomeSystemicTreatments(1, false)
        val treatments = listOf<PriorTumorTreatment>(
            TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2021).bestResponse(PD_LABEL).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateRadiological() {
        val function = HasHadPDFollowingSomeSystemicTreatments(1, true)

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one systemic with stop reason PD
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2020).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add a later systemic with other stop reason
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2021).stopReason("toxicity").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateUninterruptedTreatments() {
        val function = HasHadPDFollowingSomeSystemicTreatments(2, false)

        val treatments = listOf(
            TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2020).build(),
            TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2021).build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }
}