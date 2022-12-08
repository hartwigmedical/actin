package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class RequiresRegularHematopoieticSupportTest {

    @Test
    public void canEvaluateOnTransfusions() {
        LocalDate minDate = LocalDate.of(2020, 2, 1);
        LocalDate maxDate = minDate.plusMonths(2);

        RequiresRegularHematopoieticSupport function = new RequiresRegularHematopoieticSupport(minDate, maxDate);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(Lists.newArrayList())));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(minDate.minusWeeks(1)))));
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(maxDate.plusWeeks(1)))));

        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(create(minDate.plusMonths(1)))));
    }

    @Test
    public void canEvaluateOnMedication() {
        LocalDate minDate = LocalDate.of(2020, 2, 1);
        LocalDate maxDate = minDate.plusMonths(2);

        RequiresRegularHematopoieticSupport function = new RequiresRegularHematopoieticSupport(minDate, maxDate);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedications(Lists.newArrayList())));

        Medication tooOld = support().startDate(minDate.minusWeeks(2)).stopDate(minDate.minusWeeks(1)).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(tooOld)));

        Medication tooRecent = support().startDate(maxDate.plusWeeks(1)).stopDate(maxDate.plusWeeks(2)).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(tooRecent)));

        Medication within = support().startDate(minDate.plusWeeks(1)).stopDate(maxDate.minusWeeks(1)).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withMedication(within)));

        Medication stillRunning = support().startDate(minDate.minusWeeks(1)).stopDate(null).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withMedication(stillRunning)));

        Medication wrongCategory = TestMedicationFactory.builder().from(stillRunning).categories(Sets.newHashSet("wrong")).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withMedication(wrongCategory)));
    }

    @NotNull
    private static ImmutableMedication.Builder support() {
        return TestMedicationFactory.builder()
                .addCategories(RequiresRegularHematopoieticSupport.HEMATOPOIETIC_MEDICATION_CATEGORIES.iterator().next());
    }

    @NotNull
    private static BloodTransfusion create(@NotNull LocalDate date) {
        return ImmutableBloodTransfusion.builder().product(Strings.EMPTY).date(date).build();
    }
}