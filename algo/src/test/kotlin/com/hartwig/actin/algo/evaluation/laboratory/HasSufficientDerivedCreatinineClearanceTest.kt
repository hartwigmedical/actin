package com.hartwig.actin.algo.evaluation.laboratory

import com.google.common.collect.Lists
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
import java.time.LocalDate

class HasSufficientDerivedCreatinineClearanceTest {

    @Test
    fun canEvaluateMDRD() {
        val function = HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_MDRD, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // MDRD between 103 and 125
        val male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(male, creatinine))

        // MDRD between 73 and 95
        val female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(female, creatinine))
    }

    @Test
    fun canEvaluateCKDEPI() {
        val function = HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.EGFR_CKD_EPI, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // CDK-EPI between 104 and 125
        val male = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(male, creatinine))

        // CDK-EPI between 87 and 101
        val female = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(female, creatinine))
    }

    @Test
    fun canEvaluateCockcroftGaultWithWeight() {
        val function = HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 100.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()
        val weights: MutableList<BodyWeight> = mutableListOf()
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2020, 1, 1)).value(50.0).unit(Strings.EMPTY).build())
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 1, 1)).value(60.0).unit(Strings.EMPTY).build())

        // CG 95
        val maleLight = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(maleLight, creatinine))

        // CG 80
        val femaleLight = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(femaleLight, creatinine))
        weights.add(ImmutableBodyWeight.builder().date(LocalDate.of(2021, 2, 2)).value(70.0).unit(Strings.EMPTY).build())

        // CG 111
        val maleHeavy = create(1971, Gender.MALE, Lists.newArrayList(creatinine), weights)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(maleHeavy, creatinine))

        // CG 94
        val femaleHeavy = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), weights)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(femaleHeavy, creatinine))
    }

    @Test
    fun canEvaluateCockcroftGaultNoWeight() {
        val function = HasSufficientDerivedCreatinineClearance(2021, CreatinineClearanceMethod.COCKCROFT_GAULT, 80.0)
        val creatinine: LabValue = LabTestFactory.forMeasurement(LabMeasurement.CREATININE).value(70.0).build()

        // CG 103
        val fallBack1 = create(1971, Gender.MALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(fallBack1, creatinine))

        // CG 67
        val fallBack2 = create(1971, Gender.FEMALE, Lists.newArrayList(creatinine), Lists.newArrayList())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(fallBack2, creatinine))
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