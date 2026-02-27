package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyWeight
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDateTime
import org.junit.Test

private const val BIRTH_YEAR = 1971
private const val EXPECTED_UNIT = "kilogram"

class HasLimitedDerivedCreatinineClearanceTest {

    private val referenceDate = LocalDateTime.of(2020, 1, 1, 12, 30, 0)
    private val minimumValidDateForBodyWeight = referenceDate.minusMonths(1).toLocalDate()

    @Test
    fun `Should evaluate correctly using MDRD`() {
        val function =
            HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100.0, minimumValidDateForBodyWeight)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // MDRD between 103 and 125
        val male = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // MDRD between 73 and 95
        val female = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate correctly using CKDEPI`() {
        val function =
            HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100.0, minimumValidDateForBodyWeight)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // CDK-EPI between 104 and 125
        val male = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // CDK-EPI between 87 and 101
        val female = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate correctly using Cockcroft Gault with light weight`() {
        val function =
            HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100.0, minimumValidDateForBodyWeight)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        val weights = listOf(
            BodyWeight(date = referenceDate, value = 50.0, unit = EXPECTED_UNIT, valid = true),
            BodyWeight(date = referenceDate, value = 60.0, unit = EXPECTED_UNIT, valid = true)
        )

        // CG 95
        val maleLight = create(Gender.MALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(maleLight, LabMeasurement.CREATININE, creatinine))

        // CG 80
        val femaleLight = create(Gender.FEMALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleLight, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate correctly using Cockcroft Gault with heavy weight`() {
        val function =
            HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100.0, minimumValidDateForBodyWeight)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        val heavyWeights = listOf(BodyWeight(date = referenceDate, value = 70.0, unit = EXPECTED_UNIT, valid = true))
        // CG 111
        val maleHeavy = create(Gender.MALE, listOf(creatinine), heavyWeights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(maleHeavy, LabMeasurement.CREATININE, creatinine))

        // CG 94
        val femaleHeavy = create(Gender.FEMALE, listOf(creatinine), heavyWeights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleHeavy, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate correctly using Cockcroft Gault without weight`() {
        val function =
            HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80.0, minimumValidDateForBodyWeight)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // CG 103
        val fallBack1 = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack1, LabMeasurement.CREATININE, creatinine))

        // CG 67
        val fallBack2 = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack2, LabMeasurement.CREATININE, creatinine))
    }

    private fun create(gender: Gender, labValues: List<LabValue>, bodyWeights: List<BodyWeight>): PatientRecord {
        val base = TestPatientFactory.createMinimalTestWGSPatientRecord()
        return base.copy(
            patient = base.patient.copy(birthYear = BIRTH_YEAR, gender = gender),
            labValues = labValues,
            bodyWeights = bodyWeights
        )
    }
}