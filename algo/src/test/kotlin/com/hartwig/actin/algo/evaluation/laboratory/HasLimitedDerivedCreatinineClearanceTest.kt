package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test
import java.time.LocalDate

private const val BIRTH_YEAR = 1971

class HasLimitedDerivedCreatinineClearanceTest {

    @Test
    fun `Should evaluate MDRD`() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100.0)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // MDRD between 103 and 125
        val male = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // MDRD between 73 and 95
        val female = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate CDKEPI`() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100.0)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // CDK-EPI between 104 and 125
        val male = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // CDK-EPI between 87 and 101
        val female = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate Cockcroft-Gault with weight`() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100.0)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)
        val weights = listOf(
            BodyWeight(date = LocalDate.of(2020, 1, 1), value = 50.0, unit = ""),
            BodyWeight(date = LocalDate.of(2021, 1, 1), value = 60.0, unit = "")
        )

        // CG 95
        val maleLight = create(Gender.MALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(maleLight, LabMeasurement.CREATININE, creatinine))

        // CG 80
        val femaleLight = create(Gender.FEMALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleLight, LabMeasurement.CREATININE, creatinine))

        val heavyWeights = weights + BodyWeight(date = LocalDate.of(2021, 2, 2), value = 70.0, unit = "")

        // CG 111
        val maleHeavy = create(Gender.MALE, listOf(creatinine), heavyWeights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(maleHeavy, LabMeasurement.CREATININE, creatinine))

        // CG 94
        val femaleHeavy = create(Gender.FEMALE, listOf(creatinine), heavyWeights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleHeavy, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun `Should evaluate Cockcroft-Gault no weight`() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80.0)
        val creatinine: LabValue = LabTestFactory.create(LabMeasurement.CREATININE, 70.0)

        // CG 103
        val fallBack1 = create(Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack1, LabMeasurement.CREATININE, creatinine))

        // CG 67
        val fallBack2 = create(Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(fallBack2, LabMeasurement.CREATININE, creatinine))
    }

    private fun create(gender: Gender, labValues: List<LabValue>, bodyWeights: List<BodyWeight>): PatientRecord {
        val base = TestDataFactory.createMinimalTestPatientRecord()
        return base.copy(
            clinical = base.clinical.copy(
                patient = base.clinical.patient.copy(
                    birthYear = BIRTH_YEAR,
                    gender = gender
                ),
                labValues = labValues,
                bodyWeights = bodyWeights
            )
        )
    }
}