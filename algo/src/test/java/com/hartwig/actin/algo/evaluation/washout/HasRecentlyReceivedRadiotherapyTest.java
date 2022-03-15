package com.hartwig.actin.algo.evaluation.washout;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasRecentlyReceivedRadiotherapyTest {

    @Test
    public void canEvaluate() {
        int year = 2020;
        int month = 5;
        HasRecentlyReceivedRadiotherapy function = new HasRecentlyReceivedRadiotherapy(year, month);

        // No prior tumor treatments
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatments(Lists.newArrayList())));

        // Wrong category
        PriorTumorTreatment wrongCategory = builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(wrongCategory)));

        // Right category but no date
        PriorTumorTreatment rightCategoryNoDate = radiotherapy().build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryNoDate)));

        // Right category but old date
        PriorTumorTreatment rightCategoryOldDate = radiotherapy().year(year - 1).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(rightCategoryOldDate)));

        // Right category but old month
        PriorTumorTreatment rightCategoryOldMonth = radiotherapy().year(year).month(month - 1).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(rightCategoryOldMonth)));

        // Right category and recent year
        PriorTumorTreatment rightCategoryRecentYear = radiotherapy().year(year).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryRecentYear)));

        // Right category and recent year and month
        PriorTumorTreatment rightCategoryRecentYearAndMonth = radiotherapy().year(year).month(month).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryRecentYearAndMonth)));
    }

    @NotNull
    private static ImmutablePriorTumorTreatment.Builder radiotherapy() {
        return builder().addCategories(TreatmentCategory.RADIOTHERAPY);
    }

    @NotNull
    private static ImmutablePriorTumorTreatment.Builder builder() {
        return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);
    }

    @NotNull
    private static PatientRecord withPriorTumorTreatment(@NotNull PriorTumorTreatment treatment) {
        return withPriorTumorTreatments(Lists.newArrayList(treatment));
    }

    @NotNull
    private static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(treatments)
                        .build())
                .build();
    }
}