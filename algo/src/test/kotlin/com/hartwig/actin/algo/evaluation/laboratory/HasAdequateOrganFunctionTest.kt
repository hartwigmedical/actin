package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import org.junit.jupiter.api.Test

private val VALID_DATE = LocalDate.of(2024, 12, 1)

class HasAdequateOrganFunctionTest {

    private val function = HasAdequateOrganFunction(VALID_DATE)
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
    fun `Should evaluate to undetermined for no lab values and no comorbidities`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(labValues = emptyList(), comorbidities = emptyList())
            ),
            "Undetermined if adequate organ function (absolute neutrophil count, absolute thrombocyte count, ALAT, ASAT, estimated GFR (CKD-EPI), estimated GFR (MDRD), hemoglobin, LDH and total bilirubin undetermined)"
        )
    }

    @Test
    fun `Should evaluate to undetermined when one of the evaluated lab values is missing`() {
        val record = LabTestFactory.withLabValues(
            upperLimitLabMeasurementList.map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = false) } +
                    lowerLimitLabMeasurementList
                        .drop(1)
                        .map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = true) }
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record),
            "Undetermined if adequate organ function (hemoglobin undetermined)"
        )
    }

    @Test
    fun `Should pass when lab values within normal range and no cardiovascular disease present`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE)
        val record = LabTestFactory.withLabValues(
            upperLimitLabMeasurementList.map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = false) } +
                    lowerLimitLabMeasurementList.map { createLabValue(it, withinLimits = true, evaluateAgainstLLN = true) }
        ).copy(comorbidities = listOf(condition))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record), "No indication of inadequate organ function")
    }

    @Test
    fun `Should warn when hemoglobin, thrombocytes, neutrophils or eGFR below LLN`() {
        lowerLimitLabMeasurementList.forEach {
            val record = LabTestFactory.withLabValues(listOf(createLabValue(it, withinLimits = false, evaluateAgainstLLN = true)))
            assertEvaluation(
                EvaluationResult.WARN,
                function.evaluate(record),
                "Possible inadequate organ function (${it.display()} below LLN)"
            )
        }
    }

    @Test
    fun `Should warn when ASAT, ALAT, total bilirubin, or LD is above ULN`() {
        upperLimitLabMeasurementList.forEach {
            val record = LabTestFactory.withLabValues(
                listOf(createLabValue(it, withinLimits = false, evaluateAgainstLLN = false))
            )
            assertEvaluation(
                EvaluationResult.WARN,
                function.evaluate(record),
                "Possible inadequate organ function (${it.display()} above ULN)"
            )
        }
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
        else -> 100.0
    }
    return LabTestFactory.create(measurement, value, VALID_DATE, refLimitLow = 5.0, refLimitUp = 20.0)
}