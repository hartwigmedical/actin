package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentWithCategoryOfTypesRecentlyTest {

    @Test
    public void canEvaluate() {
        LocalDate referenceDate = LocalDate.of(2020, 4, 1);
        HasHadTreatmentWithCategoryOfTypesRecently function =
                new HasHadTreatmentWithCategoryOfTypesRecently(TreatmentCategory.TARGETED_THERAPY,
                        Lists.newArrayList("Anti-EGFR"),
                        referenceDate);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(noPriorTreatmentRecord));

        // Add one wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(immunoRecord));

        // Add one correct category but wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(multiRecord1));

        // Add one correct category with matching type
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(multiRecord2));

        // Add one correct category with matching type and recent date
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .year(referenceDate.getYear() + 1)
                .build());
        PatientRecord multiRecord3 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(multiRecord3));
    }

    @Test
    public void canDeterminePastDate() {
        LocalDate referenceDate = LocalDate.of(2020, 4, 1);

        assertNull(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().build(), referenceDate));
        assertTrue(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2021).build(),
                referenceDate));
        assertFalse(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2019).build(),
                referenceDate));
        assertNull(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2020).build(),
                referenceDate));

        assertTrue(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2020).month(7).build(),
                referenceDate));
        assertFalse(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2020).month(3).build(),
                referenceDate));
        assertNull(HasHadTreatmentWithCategoryOfTypesRecently.startedPastDate(TreatmentTestFactory.builder().year(2020).month(4).build(),
                referenceDate));
    }
}