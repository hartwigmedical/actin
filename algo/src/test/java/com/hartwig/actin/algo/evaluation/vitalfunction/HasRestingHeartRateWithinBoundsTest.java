package com.hartwig.actin.algo.evaluation.vitalfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.junit.Test;

public class HasRestingHeartRateWithinBoundsTest {

    @Test
    public void canEvaluate() {
        LocalDate referenceDate = LocalDate.of(2020, 11, 19);
        HasRestingHeartRateWithinBounds function = new HasRestingHeartRateWithinBounds(60, 80);

        List<VitalFunction> heartRates = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));

        heartRates.add(heartRate().date(referenceDate).value(70).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));

        // Fail when median drops below 60
        heartRates.add(heartRate().date(referenceDate).value(40).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));

        // Succeed again when median back in range
        heartRates.add(heartRate().date(referenceDate).value(80).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));

        // Fail again when median becomes too high
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));

        // Succeed when we add bunch of more recent correct measures.
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70).build());
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)));
    }

    private static ImmutableVitalFunction.Builder heartRate() {
        return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.HEART_RATE)
                .unit(HasRestingHeartRateWithinBounds.UNIT_TO_SELECT);

    }
}