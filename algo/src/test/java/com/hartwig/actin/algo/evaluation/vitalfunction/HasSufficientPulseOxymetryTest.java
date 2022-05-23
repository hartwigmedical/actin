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

public class HasSufficientPulseOxymetryTest {

    @Test
    public void canEvaluate() {
        LocalDate referenceDate = LocalDate.of(2021, 11, 19);
        HasSufficientPulseOxymetry function = new HasSufficientPulseOxymetry(90);

        List<VitalFunction> pulses = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        pulses.add(pulse().date(referenceDate).value(92).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        // Undetermined when the median falls below 90 but one measure above 90.
        pulses.add(pulse().date(referenceDate.minusDays(1)).value(80).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        // Succeed when median goes above 90.
        pulses.add(pulse().date(referenceDate).value(92).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        // Still succeed again with multiple more good results.
        pulses.add(pulse().date(referenceDate.minusDays(2)).value(98).build());
        pulses.add(pulse().date(referenceDate.minusDays(3)).value(99).build());
        pulses.add(pulse().date(referenceDate.minusDays(4)).value(98).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        // Still succeed since we only take X most recent measures.
        pulses.add(pulse().date(referenceDate.minusDays(5)).value(20).build());
        pulses.add(pulse().date(referenceDate.minusDays(6)).value(20).build());
        pulses.add(pulse().date(referenceDate.minusDays(7)).value(20).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));

        // Fail if we add more recent measures that are too low
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20).build());
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20).build());
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20).build());
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20).build());
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)));
    }

    @NotNull
    private static ImmutableVitalFunction.Builder pulse() {
        return VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2);
    }
}