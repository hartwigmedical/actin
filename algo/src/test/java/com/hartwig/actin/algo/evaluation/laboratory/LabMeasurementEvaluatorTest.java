package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabMeasurementEvaluatorTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2020, 4, 20);
    private static final LocalDate ALWAYS_VALID_DATE = TEST_DATE.minusDays(2);

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;
        LabMeasurementEvaluator function = new LabMeasurementEvaluator(measurement,
                (x, y) -> EvaluationTestFactory.withResult(EvaluationResult.PASS),
                ALWAYS_VALID_DATE);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        LabValue labValue = LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(LabTestFactory.withLabValue(labValue)));
    }

    @Test
    public void canIgnoreOldDatesAndInvalidUnits() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;
        LabMeasurementEvaluator function =
                new LabMeasurementEvaluator(measurement, (x, y) -> EvaluationTestFactory.withResult(EvaluationResult.PASS), TEST_DATE);

        LabValue wrongUnit = LabTestFactory.builder().code(measurement.code()).date(TEST_DATE).build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(wrongUnit)));

        LabValue oldDate = LabTestFactory.forMeasurement(measurement).date(TEST_DATE.minusDays(1)).build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(oldDate)));
    }

    @Test
    public void canFallbackToSecondMostRecent() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;

        List<LabValue> values = Lists.newArrayList();
        values.add(LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build());
        values.add(LabTestFactory.forMeasurement(measurement).date(TEST_DATE.minusDays(1)).build());
        PatientRecord record = LabTestFactory.withLabValues(values);

        LabMeasurementEvaluator functionPass =
                new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(EvaluationResult.PASS), ALWAYS_VALID_DATE);
        assertEvaluation(EvaluationResult.UNDETERMINED, functionPass.evaluate(record));

        LabMeasurementEvaluator functionFail =
                new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(EvaluationResult.FAIL), ALWAYS_VALID_DATE);
        assertEvaluation(EvaluationResult.FAIL, functionFail.evaluate(record));

        LabMeasurementEvaluator functionUndetermined =
                new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(EvaluationResult.UNDETERMINED), ALWAYS_VALID_DATE);
        assertEvaluation(EvaluationResult.FAIL, functionUndetermined.evaluate(record));
    }

    @NotNull
    private static LabEvaluationFunction firstFailAndRestWithParam(@NotNull EvaluationResult defaultEvaluation) {
        return (record, labValue) -> {
            if (labValue.date().equals(TEST_DATE)) {
                return EvaluationTestFactory.withResult(EvaluationResult.FAIL);
            } else {
                return EvaluationTestFactory.withResult(defaultEvaluation);
            }
        };
    }
}