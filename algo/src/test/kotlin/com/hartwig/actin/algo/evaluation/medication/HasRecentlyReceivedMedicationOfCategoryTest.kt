package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedMedicationOfCategoryTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("wrong category").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).stopDate(EVALUATION_DATE.plusDays(1)).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldBeUndeterminedWhenMedicationStoppedAfterMinStopDate() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).stopDate(EVALUATION_DATE.plusWeeks(2)).build())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1)
        private val FUNCTION = HasRecentlyReceivedMedicationOfCategory(
            MedicationTestFactory.alwaysStopped(), setOf("category to find"),
            EVALUATION_DATE
        )
    }
}