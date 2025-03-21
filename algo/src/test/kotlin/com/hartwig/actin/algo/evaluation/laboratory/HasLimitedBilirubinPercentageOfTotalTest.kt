package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotalTest {

    @Test
    fun canEvaluate() {
        val function = HasLimitedBilirubinPercentageOfTotal(50.0, LocalDate.of(2020, 3, 3))
        val validBilirubin: LabValue =
            LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, 10.0, LocalDate.of(2020, 4, 4))
        val valid = LabTestFactory.withLabValue(validBilirubin)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(valid, LabMeasurement.DIRECT_BILIRUBIN, LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, 3.0))
        )
        val actual = function.evaluate(valid, LabMeasurement.DIRECT_BILIRUBIN, LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, 8.0))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertThat(actual.recoverable).isTrue

        // Cannot determine if no total bilirubin
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord(),
                LabMeasurement.DIRECT_BILIRUBIN,
                LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN)
            )
        )

        // Cannot determine in case of old bilirubin.
        val invalidBilirubin: LabValue = LabTestFactory.create(LabMeasurement.TOTAL_BILIRUBIN, 10.0, LocalDate.of(2019, 4, 4))
        val invalid = LabTestFactory.withLabValue(invalidBilirubin)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(invalid, LabMeasurement.DIRECT_BILIRUBIN, LabTestFactory.create(LabMeasurement.DIRECT_BILIRUBIN, 3.0))
        )
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnWrongInputLabValue() {
        val function = HasLimitedBilirubinPercentageOfTotal(50.0, LocalDate.of(2020, 3, 3))
        function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord(),
            LabMeasurement.ALBUMIN,
            LabTestFactory.create(LabMeasurement.ALBUMIN)
        )
    }
}