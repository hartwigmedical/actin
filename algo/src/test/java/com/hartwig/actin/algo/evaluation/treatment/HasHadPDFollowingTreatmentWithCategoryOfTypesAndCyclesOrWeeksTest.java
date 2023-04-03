package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import static com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.PD_LABEL;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTest {

    @Test
    public void canEvaluate() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function = function();

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).stopReason(PD_LABEL).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type but no PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason("toxicity")
                .bestResponse("improved")
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and missing type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason(PD_LABEL).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type and missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category, type and stop reason PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason(PD_LABEL)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void shouldPassForMatchingTreatmentWhenPDIsIndicatedInBestResponse() {
        List<PriorTumorTreatment> treatments = Collections.singletonList(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .bestResponse(PD_LABEL)
                .build());
        assertEvaluation(EvaluationResult.PASS, function().evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateWithTrials() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function().evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build())));
    }

    @Test
    public void canEvaluateWithCycles() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function = function(5, null);

        // Cycles not configured
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .build())));

        // Not enough cycles
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .cycles(3)
                        .build())));

        // Sufficient number of cycles
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .cycles(7)
                        .build())));
    }

    @Test
    public void canEvaluateWithWeeks() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function = function(null, 5);

        // Dates not configured
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .build())));

        // Not enough weeks
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .startYear(1)
                        .startMonth(3)
                        .stopYear(1)
                        .stopMonth(5)
                        .build())));

        // Sufficient number of weeks
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(PD_LABEL)
                        .chemoType("type 1")
                        .startYear(1)
                        .startMonth(3)
                        .stopYear(1)
                        .stopMonth(12)
                        .build())));
    }

    private static HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function() {
        return function(null, null);
    }

    private static HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function(@Nullable Integer minCycles,
            @Nullable Integer minWeeks) {
        return new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(TreatmentCategory.CHEMOTHERAPY,
                Collections.singletonList("type 1"),
                minCycles,
                minWeeks);
    }
}