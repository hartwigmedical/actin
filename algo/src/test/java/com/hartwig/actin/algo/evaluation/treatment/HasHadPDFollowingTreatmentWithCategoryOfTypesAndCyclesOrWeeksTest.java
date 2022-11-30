package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeksTest {

    @Test
    public void canEvaluate() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function =
                new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(TreatmentCategory.CHEMOTHERAPY,
                        Lists.newArrayList("type 1"),
                        null,
                        null);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type but no PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason("toxicity")
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and missing type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason("toxicity").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type and missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category, type and stop reason PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateWithTrials() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function =
                new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(TreatmentCategory.CHEMOTHERAPY,
                        Lists.newArrayList("type 1"),
                        null,
                        null);

        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build())));
    }

    @Test
    public void canEvaluateWithCycles() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function =
                new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(TreatmentCategory.CHEMOTHERAPY,
                        Lists.newArrayList("type 1"),
                        5,
                        null);

        // Cycles not configured
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .build())));

        // Not enough cycles
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .cycles(3)
                        .build())));

        // Sufficient number of cycles
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .cycles(7)
                        .build())));
    }

    @Test
    public void canEvaluateWithWeeks() {
        HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks function =
                new HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(TreatmentCategory.CHEMOTHERAPY,
                        Lists.newArrayList("type 1"),
                        null,
                        5);

        // Dates not configured
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .build())));

        // Not enough weeks TODO: fix test
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .startYear(1)
                        .startMonth(3)
                        .stopYear(1)
                        .stopMonth(5)
                        .build())));

        // Sufficient number of cycles TODO: fix test
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.CHEMOTHERAPY)
                        .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks.STOP_REASON_PD)
                        .chemoType("type 1")
                        .startYear(1)
                        .startMonth(3)
                        .stopYear(1)
                        .stopMonth(12)
                        .build())));
    }
}