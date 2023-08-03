package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTest {
    @Test
    fun canEvaluate() {
        val function = function()
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and type but no PD
        treatments.add(
            TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").stopReason("toxicity")
                .bestResponse("improved").build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and missing type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and type and missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category, type and stop reason PD
        treatments.add(
            TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").stopReason(PD_LABEL).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val treatments = listOf(
            TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").bestResponse(PD_LABEL).build()
        )
        assertEvaluation(EvaluationResult.PASS, function().evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function().evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build()
                )
            )
        )
    }

    @Test
    fun shouldNotCountTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.SURGERY, listOf("type 1"),
            null, null
        )
        val trial = TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(listOf(trial, trial)))
        )
    }

    @Test
    fun canEvaluateWithCycles() {
        val function = function(5, null)

        // Cycles not configured
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .build()
                )
            )
        )

        // Not enough cycles
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).chemoType("type 1")
                        .cycles(3).build()
                )
            )
        )

        // Sufficient number of cycles
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).chemoType("type 1")
                        .cycles(7).build()
                )
            )
        )
    }

    @Test
    fun canEvaluateWithWeeks() {
        val function = function(null, 5)

        // Dates not configured
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).chemoType("type 1")
                        .build()
                )
            )
        )

        // Not enough weeks
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).chemoType("type 1")
                        .startYear(1).startMonth(3).stopYear(1).stopMonth(5).build()
                )
            )
        )

        // Sufficient number of weeks
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatment(
                    TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).chemoType("type 1")
                        .startYear(1).startMonth(3).stopYear(1).stopMonth(12).build()
                )
            )
        )
    }

    companion object {
        private fun function(minCycles: Int? = null, minWeeks: Int? = null): HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks {
            return HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
                TreatmentCategory.CHEMOTHERAPY, listOf("type 1"), minCycles, minWeeks
            )
        }
    }
}