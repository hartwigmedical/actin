package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

private const val MIN_CREATININE_CLEARANCE = 60.0

class HasSufficientMeasuredCreatinineClearanceTest {

    @Test
    fun `Should pass for DAILY_TOTAL when calculated clearance is above minimum`() {
        // CrCl = 10 * 1000 * 1000 / (70 * 1440) = 99.2 → above min of 60
        assertEvaluation(EvaluationResult.PASS, evaluateFormula(MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
            LabMeasurement.CREATININE_24U to 10.0, LabMeasurement.CREATININE to 70.0))
    }

    @Test
    fun `Should fail for DAILY_TOTAL when calculated clearance is below minimum`() {
        // CrCl = 5 * 1000 * 1000 / (150 * 1440) = 23.1 → below min of 60
        assertEvaluation(EvaluationResult.FAIL, evaluateFormula(MeasuredCreatinineClearanceMethod.DAILY_TOTAL,
            LabMeasurement.CREATININE_24U to 5.0, LabMeasurement.CREATININE to 150.0))
    }

    @Test
    fun `Should pass for URINE_CONCENTRATION when calculated clearance is above minimum`() {
        // CrCl = 5 * 1500 * 1000 / (70 * 1440) = 74.4 → above min of 60
        assertEvaluation(EvaluationResult.PASS, evaluateFormula(MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION,
            LabMeasurement.CREATININE_URINE to 5.0, LabMeasurement.URINE_VOLUME_24H to 1500.0, LabMeasurement.CREATININE to 70.0))
    }

    @Test
    fun `Should fail for URINE_CONCENTRATION when calculated clearance is below minimum`() {
        // CrCl = 2 * 1000 * 1000 / (150 * 1440) = 9.3 → below min of 60
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

    @ParameterizedTest
    @EnumSource(MeasuredCreatinineClearanceMethod::class)
    internal fun `Should be undetermined when CREATININE value is zero`(method: MeasuredCreatinineClearanceMethod) {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFormula(method,
                LabMeasurement.CREATININE_24U to 10.0, LabMeasurement.CREATININE_URINE to 5.0,
                LabMeasurement.URINE_VOLUME_24H to 1500.0, LabMeasurement.CREATININE to 0.0)
        )
    }

    private fun evaluateFormula(
        method: MeasuredCreatinineClearanceMethod,
        vararg pairs: Pair<LabMeasurement, Double>,
        comparatorOverrides: Map<LabMeasurement, String> = emptyMap(),
        minValue: Double = MIN_CREATININE_CLEARANCE,
    ) = HasSufficientMeasuredCreatinineClearance(minValue, method).evaluate(
        LabTestFactory.withLabValues(emptyList()),
        pairs.associate { (measurement, value) ->
            measurement to LabTestFactory.create(measurement, value = value).let {
                comparatorOverrides[measurement]?.let { comparator -> it.copy(comparator = comparator) } ?: it
            }
        }
    )
}
