package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test
import java.time.LocalDate

class HasLimitedBilirubinPercentageOfTotalTest {
    @Test
    fun canEvaluate() {
        val function = HasLimitedBilirubinPercentageOfTotal(50.0, LocalDate.of(2020, 3, 3))
        val validBilirubin: LabValue =
            LabTestFactory.forMeasurement(LabMeasurement.TOTAL_BILIRUBIN).date(LocalDate.of(2020, 4, 4)).value(10.0).build()
        val valid = LabTestFactory.withLabValue(validBilirubin)
        val directBili = LabTestFactory.forMeasurement(LabMeasurement.DIRECT_BILIRUBIN)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(valid, directBili.value(3.0).build()))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(valid, directBili.value(8.0).build()))

        // Cannot determine if no total bilirubin
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestDataFactory.createMinimalTestPatientRecord(), directBili.build())
        )

        // Cannot determine in case of old bilirubin.
        val invalidBilirubin: LabValue =
            LabTestFactory.forMeasurement(LabMeasurement.TOTAL_BILIRUBIN).date(LocalDate.of(2019, 4, 4)).value(10.0).build()
        val invalid = LabTestFactory.withLabValue(invalidBilirubin)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(invalid, directBili.value(3.0).build()))
    }

    @Test(expected = IllegalStateException::class)
    fun crashOnWrongInputLabValue() {
        val function = HasLimitedBilirubinPercentageOfTotal(50.0, LocalDate.of(2020, 3, 3))
        function.evaluate(TestDataFactory.createMinimalTestPatientRecord(), LabTestFactory.forMeasurement(LabMeasurement.ALBUMIN).build())
    }
}