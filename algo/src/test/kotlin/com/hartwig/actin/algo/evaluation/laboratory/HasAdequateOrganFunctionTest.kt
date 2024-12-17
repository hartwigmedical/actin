package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import java.time.LocalDate

private val VALID_DATE = LocalDate.of(2024, 12, 1)

class HasAdequateOrganFunctionTest {

    private val function = HasAdequateOrganFunction(VALID_DATE, TestDoidModelFactory.createMinimalTestDoidModel())
    private val upperLimitLabMeasurementList = listOf(
        LabMeasurement.LACTATE_DEHYDROGENASE,
        LabMeasurement.TOTAL_BILIRUBIN,
        LabMeasurement.ALANINE_AMINOTRANSFERASE,
        LabMeasurement.ASPARTATE_AMINOTRANSFERASE
    )
    private val lowerLimitLabMeasurementList = listOf(
        LabMeasurement.HEMOGLOBIN,
        LabMeasurement.THROMBOCYTES_ABS,
        LabMeasurement.NEUTROPHILS_ABS,
        LabMeasurement.EGFR_MDRD,
        LabMeasurement.EGFR_CKD_EPI
    )

    @Test
    fun `Should fail for empty record`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(labValues = emptyList(), priorOtherConditions = emptyList())
            )
        )
    }

    @Test
    fun `Should fail when lab values within normal range and no cardiovascular disease present`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(doids = setOf(DoidConstants.STOMACH_DISEASE_DOID))
        val record = LabTestFactory.withLabValues(
            upperLimitLabMeasurementList.map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = false) } +
                    lowerLimitLabMeasurementList.map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = true) }
        ).copy(priorOtherConditions = listOf(condition))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should warn when hemoglobin, thrombocytes, neutrophils or eGFR below LLN`() {
        lowerLimitLabMeasurementList.forEach {
            val record = LabTestFactory.withLabValues(listOf(createLabValue(it, withinLimits = false, evaluateAgainstLLN = true)))
            assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
        }
    }

    @Test
    fun `Should warn when ASAT, ALAT, total bilirubin, or LD is above ULN`() {
        upperLimitLabMeasurementList.forEach {
            val record = LabTestFactory.withLabValues(listOf(createLabValue(it, withinLimits = false, evaluateAgainstLLN = false)))
            assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
        }
    }

    @Test
    fun `Should warn when cardiovascular disease present in history`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.priorOtherCondition(doids = setOf(DoidConstants.CARDIOVASCULAR_DISEASE_DOID))
                )
            )
        )
    }
}

private fun createLabValue(
    measurement: LabMeasurement,
    withinLimits: Boolean,
    evaluateAgainstLLN: Boolean
): LabValue {
    val value = when {
        withinLimits -> 10.0
        evaluateAgainstLLN -> 1.0
        else -> 25.0
    }
    return LabTestFactory.create(measurement, value, VALID_DATE, refLimitLow = 5.0, refLimitUp = 20.0)
}