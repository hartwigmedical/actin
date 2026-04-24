package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.junit.jupiter.api.Test

class HasSufficientMeasuredCreatinineClearanceTest {

    @Test
    fun `Should pass for DAILY_TOTAL when calculated clearance is above minimum`() {
        assertEvaluation(EvaluationResult.PASS, evaluateFormula(MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
            LabMeasurement.CREATININE_24U to 10.0, LabMeasurement.CREATININE to 70.0))
    }

    @Test
    fun `Should fail for DAILY_TOTAL when calculated clearance is below minimum`() {
        assertEvaluation(EvaluationResult.FAIL, evaluateFormula(MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
            LabMeasurement.CREATININE_24U to 5.0, LabMeasurement.CREATININE to 150.0))
    }

    @Test
    fun `Should pass for URINE_CONCENTRATION when calculated clearance is above minimum`() {
        assertEvaluation(EvaluationResult.PASS, evaluateFormula(MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
            LabMeasurement.CREATININE_URINE to 5.0, LabMeasurement.URINE_VOLUME_24H to 1500.0, LabMeasurement.CREATININE to 70.0))
    }

    @Test
    fun `Should fail for URINE_CONCENTRATION when calculated clearance is below minimum`() {
        assertEvaluation(EvaluationResult.FAIL, evaluateFormula(MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
            LabMeasurement.CREATININE_URINE to 2.0, LabMeasurement.URINE_VOLUME_24H to 1000.0, LabMeasurement.CREATININE to 150.0))
    }

    @Test
    fun `Should be undetermined for DAILY_TOTAL when CREATININE is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
                LabMeasurement.CREATININE_24U to 10.0
            )
        )
    }

    @Test
    fun `Should be undetermined for DAILY_TOTAL when CREATININE_24U is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
                LabMeasurement.CREATININE to 70.0
            )
        )
    }

    @Test
    fun `Should be undetermined for URINE_CONCENTRATION when CREATININE is missing`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluateFormula(
            MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
            LabMeasurement.CREATININE_URINE to 5.0,
            LabMeasurement.URINE_VOLUME_24H to 1500.0
        ))
    }

    @Test
    fun `Should be undetermined for URINE_CONCENTRATION when CREATININE_URINE is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
                LabMeasurement.URINE_VOLUME_24H to 1500.0, LabMeasurement.CREATININE to 70.0
            )
        )
    }

    @Test
    fun `Should be undetermined for URINE_CONCENTRATION when URINE_VOLUME_24H is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
                LabMeasurement.CREATININE_URINE to 5.0, LabMeasurement.CREATININE to 70.0
            )
        )
    }

    @Test
    fun `Should be undetermined for DAILY_TOTAL when any input has a comparator`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
                LabMeasurement.CREATININE_24U to 10.0, LabMeasurement.CREATININE to 70.0,
                comparatorOverrides = mapOf(LabMeasurement.CREATININE_24U to ">")
            )
        )
    }

    @Test
    fun `Should be undetermined for URINE_CONCENTRATION when any input has a comparator`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(
                MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
                LabMeasurement.CREATININE_URINE to 5.0, LabMeasurement.URINE_VOLUME_24H to 1500.0, LabMeasurement.CREATININE to 70.0,
                comparatorOverrides = mapOf(LabMeasurement.CREATININE to "<")
            )
        )
    }

    private fun evaluateFormula(
        method: MeasuredCreatinineClearanceMethod,
        vararg pairs: Pair<LabMeasurement, Double>,
        comparatorOverrides: Map<LabMeasurement, String> = emptyMap(),
        minValue: Double = 60.0,
    ) = HasSufficientMeasuredCreatinineClearance(minValue, method).evaluate(
        LabTestFactory.withLabValues(emptyList()),
        pairs.associate { (measurement, value) ->
            measurement to LabTestFactory.create(measurement, value = value).let {
                comparatorOverrides[measurement]?.let { comparator -> it.copy(comparator = comparator) } ?: it
            }
        }
    )
}
