package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.HeartMeasurement
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.jupiter.api.Test

class HasEcgAberrationTest {
    private val function = HasEcgAberration(TestIcdFactory.createTestModel())

    @Test
    fun `Should pass with ECG aberration`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withEcgDescription("with description")),
            "ECG abnormalities present (with description)"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withEcgDescription(null)),
            "ECG abnormalities present (details unknown)"
        )
    }

    @Test
    fun `Should pass with cardiac arrhythmia in history`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ComorbidityTestFactory.withComorbidity(otherCondition(icdMainCode = IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK))),
            "Cardiac arrhythmia in history ()"
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withEcgDescription(null)),
            "ECG abnormalities present (details unknown)"
        )
    }

    @Test
    fun `Should pass with ECG aberration and cardiac arrhythmia in history`() {
        val record = ComorbidityTestFactory.withComorbidities(
            listOf(
                otherCondition(
                    name = "cardiac arrhythmia",
                    icdMainCode = IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK
                ), HeartMeasurement("ecg abnormality", emptySet(), null, "", HeartMeasurementType.OTHER_ECG)
            )
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(
            EvaluationResult.PASS,
            evaluation,
            "ECG abnormalities (ecg abnormality) and cardiac arrhythmia (cardiac arrhythmia) in history"
        )
    }

    @Test
    fun `Should fail with no ECG aberration no cardiac arrhythmia comorbidities`() {
        val record = CardiacFunctionTestFactory.withEcg(null).copy(comorbidities = emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record), "No known ECG abnormalities")
    }
}