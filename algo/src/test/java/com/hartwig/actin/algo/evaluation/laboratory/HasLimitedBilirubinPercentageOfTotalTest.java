package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLimitedBilirubinPercentageOfTotalTest {

    @Test
    public void canEvaluate() {
        HasLimitedBilirubinPercentageOfTotal function = new HasLimitedBilirubinPercentageOfTotal(50D, LocalDate.of(2020, 3, 3));

        LabValue validBilirubin =
                LabTestFactory.forMeasurement(LabMeasurement.TOTAL_BILIRUBIN).date(LocalDate.of(2020, 4, 4)).value(10).build();
        PatientRecord valid = LabTestFactory.withLabValue(validBilirubin);

        ImmutableLabValue.Builder directBili = LabTestFactory.forMeasurement(LabMeasurement.DIRECT_BILIRUBIN);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(valid, directBili.value(3D).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(valid, directBili.value(8D).build()));

        // Cannot determine if no total bilirubin
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TestDataFactory.createMinimalTestPatientRecord(), directBili.build()));

        // Cannot determine in case of old bilirubin.
        LabValue invalidBilirubin =
                LabTestFactory.forMeasurement(LabMeasurement.TOTAL_BILIRUBIN).date(LocalDate.of(2019, 4, 4)).value(10).build();
        PatientRecord invalid = LabTestFactory.withLabValue(invalidBilirubin);
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(invalid, directBili.value(3D).build()));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnWrongInputLabValue() {
        HasLimitedBilirubinPercentageOfTotal function = new HasLimitedBilirubinPercentageOfTotal(50D, LocalDate.of(2020, 3, 3));

        function.evaluate(TestDataFactory.createMinimalTestPatientRecord(), LabTestFactory.forMeasurement(LabMeasurement.ALBUMIN).build());
    }
}