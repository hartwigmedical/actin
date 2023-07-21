package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

class HasHadTreatmentWithCategoryOfTypesRecentlyTest {
    @Test
    fun canEvaluate() {
        val minDate = LocalDate.of(2020, 4, 1)
        val function = HasHadTreatmentWithCategoryOfTypesRecently(
            TreatmentCategory.TARGETED_THERAPY,
            listOf("Anti-EGFR"),
            minDate
        )

        // No treatments yet
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one correct category but no type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one correct category with matching type but long time ago.
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .startYear(minDate.year - 1)
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one correct category with matching type
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Add one correct category with matching type and recent date
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .startYear(minDate.year + 1)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val minDate = LocalDate.of(2020, 4, 1)
        val function = HasHadTreatmentWithCategoryOfTypesRecently(
            TreatmentCategory.TARGETED_THERAPY,
            listOf("Anti-EGFR"),
            minDate
        )
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TRIAL)
                .startYear(minDate.minusYears(1).year)
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TRIAL)
                .startYear(minDate.plusYears(1).year)
                .build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldNotCountTrialMatchesWhenLookingForUnlikelyTrialCategories() {
        val minDate = LocalDate.of(2020, 4, 1)
        val function = HasHadTreatmentWithCategoryOfTypesRecently(TreatmentCategory.SURGERY, listOf("type 1"), minDate)
        val trial = TreatmentTestFactory.builder()
            .addCategories(TreatmentCategory.TRIAL)
            .startYear(minDate.plusYears(1).year)
            .build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(listOf(trial, trial))))
    }
}