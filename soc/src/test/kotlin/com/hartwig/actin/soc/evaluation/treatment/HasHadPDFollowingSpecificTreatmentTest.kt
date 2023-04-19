package com.hartwig.actin.soc.evaluation.treatment

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.soc.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.soc.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL
import org.junit.Test

class HasHadPDFollowingSpecificTreatmentTest {

    @Test
    fun canEvaluate() {
        val function = HasHadPDFollowingSpecificTreatment(Sets.newHashSet("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY)
        val treatments: MutableList<PriorTumorTreatment> = Lists.newArrayList<PriorTumorTreatment>()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category but no correct name
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("other").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and name but missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("treatment 1").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category, name and stop reason PD
        treatments.add(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .name("treatment 1")
                .stopReason(PD_LABEL)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        val function = HasHadPDFollowingSpecificTreatment(Sets.newHashSet("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY)
        val treatments: List<PriorTumorTreatment> = listOf(
            TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .name("treatment 1")
                .bestResponse(PD_LABEL)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateStopReasons() {
        val treatment = "right treatment"
        val function = HasHadPDFollowingSpecificTreatment(Sets.newHashSet(treatment), null)
        val treatments: MutableList<PriorTumorTreatment> = Lists.newArrayList<PriorTumorTreatment>()

        // Right category but different stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason("toxicity").bestResponse("improved").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category but no stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason(null).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))

        // Right category and right stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason(PD_LABEL).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)))
    }

    @Test
    fun canEvaluateWithTrials() {
        val function = HasHadPDFollowingSpecificTreatment(Sets.newHashSet("right treatment"), null)

        // Add trial
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