package com.hartwig.actin.algo.evaluation.vitalfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasLimitedBloodPressureTest {

    @Test
    public void canEvaluate() {
        LocalDate referenceDate = LocalDate.of(2020, 11, 19);
        HasLimitedBloodPressure function = new HasLimitedBloodPressure(BloodPressureCategory.SYSTOLIC, 100);

        List<VitalFunction> bloodPressures = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));

        bloodPressures.add(systolic().date(referenceDate).value(90).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));

        // Undetermined when the median raises above 100 but one measure below 100
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(120).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));

        // Succeed again when the median drops below 100
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(90).build());
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(90).build());
        bloodPressures.add(systolic().date(referenceDate.minusDays(4)).value(90).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));

        // Still succeed since we only take X most recent measures.
        bloodPressures.add(systolic().date(referenceDate.minusDays(5)).value(200).build());
        bloodPressures.add(systolic().date(referenceDate.minusDays(6)).value(200).build());
        bloodPressures.add(systolic().date(referenceDate.minusDays(7)).value(200).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));

        // Fail when we have lots of high measures prior to evaluation
        bloodPressures.add(systolic().date(referenceDate.plusDays(1)).value(110).build());
        bloodPressures.add(systolic().date(referenceDate.plusDays(2)).value(110).build());
        bloodPressures.add(systolic().date(referenceDate.plusDays(3)).value(110).build());
        bloodPressures.add(systolic().date(referenceDate.plusDays(4)).value(110).build());
        bloodPressures.add(systolic().date(referenceDate.plusDays(5)).value(110).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));
    }

    @Test
    public void canFilterOnCategory() {
        LocalDate referenceDate = LocalDate.of(2020, 11, 19);
        HasLimitedBloodPressure function = new HasLimitedBloodPressure(BloodPressureCategory.SYSTOLIC, 100);

        List<VitalFunction> bloodPressures = Lists.newArrayList();
        bloodPressures.add(diastolic().date(referenceDate).value(90).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)));
    }

    @NotNull
    private static ImmutableVitalFunction.Builder systolic() {
        return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.SYSTOLIC.display());
    }

    @NotNull
    private static ImmutableVitalFunction.Builder diastolic() {
        return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.DIASTOLIC.display());
    }
}