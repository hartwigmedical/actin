package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabMeasurementEvaluatorTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2020, 4, 20);

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;
        LabMeasurementEvaluator function = new LabMeasurementEvaluator(measurement, x -> Evaluation.PASS);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        LabValue wrongUnit = LabTestFactory.builder().code(measurement.code()).build();
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(LabTestFactory.withLabValue(wrongUnit)));

        LabValue correct = LabTestFactory.forMeasurement(measurement).build();
        assertEquals(Evaluation.PASS, function.evaluate(LabTestFactory.withLabValue(correct)));
    }

    @Test
    public void canFallbackToSecondMostRecent() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;

        List<LabValue> values = Lists.newArrayList();
        values.add(LabTestFactory.forMeasurement(measurement).date(TEST_DATE).build());
        values.add(LabTestFactory.forMeasurement(measurement).date(TEST_DATE.minusDays(1)).build());
        PatientRecord record = LabTestFactory.withLabValueList(values);

        LabMeasurementEvaluator functionPass = new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(Evaluation.PASS));
        assertEquals(Evaluation.UNDETERMINED, functionPass.evaluate(record));

        LabMeasurementEvaluator functionFail = new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(Evaluation.FAIL));
        assertEquals(Evaluation.FAIL, functionFail.evaluate(record));

        LabMeasurementEvaluator functionUndetermined =
                new LabMeasurementEvaluator(measurement, firstFailAndRestWithParam(Evaluation.UNDETERMINED));
        assertEquals(Evaluation.FAIL, functionUndetermined.evaluate(record));
    }

    @NotNull
    private static LabEvaluationFunction firstFailAndRestWithParam(@NotNull Evaluation defaultEvaluation) {
        return value -> {
            if (value.date().equals(TEST_DATE)) {
                return Evaluation.FAIL;
            } else {
                return defaultEvaluation;
            }
        };
    }
}