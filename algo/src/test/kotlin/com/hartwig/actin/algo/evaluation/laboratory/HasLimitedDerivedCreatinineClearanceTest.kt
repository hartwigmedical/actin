package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDateTime

class HasLimitedDerivedCreatinineClearanceTest {
    @Test
    fun canEvaluateMDRD() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // MDRD between 103 and 125
        val male = create(1971, Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // MDRD between 73 and 95
        val female = create(1971, Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun canEvaluateCKDEPI() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // CDK-EPI between 104 and 125
        val male = create(1971, Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(male, LabMeasurement.CREATININE, creatinine))

        // CDK-EPI between 87 and 101
        val female = create(1971, Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(female, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun canEvaluateCockcroftGaultWithWeight() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()
        val weights = mutableListOf(
            ImmutableBodyWeight.builder().date(LocalDateTime.of(2020, 1, 1, 12, 30, 0)).value(50.0).unit(Strings.EMPTY).valid(true).build(),
            ImmutableBodyWeight.builder().date(LocalDateTime.of(2021, 1, 1, 12, 30, 0)).value(60.0).unit(Strings.EMPTY).valid(true).build()
        )

        // CG 95
        val maleLight = create(1971, Gender.MALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(maleLight, LabMeasurement.CREATININE, creatinine))

        // CG 80
        val femaleLight = create(1971, Gender.FEMALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleLight, LabMeasurement.CREATININE, creatinine))
        weights.add(
            ImmutableBodyWeight.builder().date(LocalDateTime.of(2021, 2, 2, 12, 30, 0)).value(70.0).unit(Strings.EMPTY).valid(true).build()
        )

        // CG 111
        val maleHeavy = create(1971, Gender.MALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(maleHeavy, LabMeasurement.CREATININE, creatinine))

        // CG 94
        val femaleHeavy = create(1971, Gender.FEMALE, listOf(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(femaleHeavy, LabMeasurement.CREATININE, creatinine))
    }

    @Test
    fun canEvaluateCockcroftGaultNoWeight() {
        val function = HasLimitedDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // CG 103
        val fallBack1 = create(1971, Gender.MALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack1, LabMeasurement.CREATININE, creatinine))

        // CG 67
        val fallBack2 = create(1971, Gender.FEMALE, listOf(creatinine), emptyList())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(fallBack2, LabMeasurement.CREATININE, creatinine))
    }

    companion object {
        private fun create(birthYear: Int, gender: Gender, labValues: List<LabValue>, bodyWeights: List<BodyWeight>): PatientRecord {
            val base = TestClinicalFactory.createMinimalTestClinicalRecord()
            return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(base)
                        .patient(ImmutablePatientDetails.builder().from(base.patient()).birthYear(birthYear).gender(gender).build())
                        .labValues(labValues)
                        .bodyWeights(bodyWeights)
                        .build()
                )
                .build()
        }
    }
}