package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDateTime
import org.junit.Test

class HasSufficientMeasuredCreatinineClearanceTest {

    private val referenceDate = LocalDateTime.of(2020, 1, 1, 12, 30, 0)
    private val minimumValidDateForBodyWeight = referenceDate.minusMonths(1).toLocalDate()

    @Test
    fun `Should pass when measured creatinine clearance is sufficient`() {
        val function =
            HasSufficientMeasuredCreatinineClearance( 90.0, minimumValidDateForBodyWeight, minimumValidDateForBodyWeight)
        val serumCreatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 69.0)
        val urineCreatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE_URINE, 6.0)
        val urineVolume: LabValue = LabTestFactory.create(LabMeasurement.URINE_VOLUME_24H, 1500.0)
        val patientRecord = create(listOf(serumCreatinine, urineCreatinine, urineVolume))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when measured creatinine clearance is insufficient`() {
        val function =
            HasSufficientMeasuredCreatinineClearance( 90.0, minimumValidDateForBodyWeight, minimumValidDateForBodyWeight)
        val serumCreatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 69.0)
        val urineCreatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE_URINE, 3.0)
        val urineVolume: LabValue = LabTestFactory.create(LabMeasurement.URINE_VOLUME_24H, 1500.0)
        val patientRecord = create(listOf(serumCreatinine, urineCreatinine, urineVolume))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    private fun create(labValues: List<LabValue>): PatientRecord {
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(labValues = labValues)
    }
}
