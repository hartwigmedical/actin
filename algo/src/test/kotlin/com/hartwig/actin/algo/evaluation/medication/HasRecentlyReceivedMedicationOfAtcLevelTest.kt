package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedMedicationOfAtcLevelTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(ImmutableAtcLevel.builder().code("category to find").name("").build()),
            EVALUATION_DATE.minusDays(1)
        )
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).stopDate(EVALUATION_DATE).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldBeUndeterminedWhenMedicationStoppedAfterMinStopDate() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(ImmutableAtcLevel.builder().code("category to find").name("").build()),
            EVALUATION_DATE.minusWeeks(2)
        )
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category to find").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).stopDate(EVALUATION_DATE).build())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1)
        private val FUNCTION_ACTIVE = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysActive(),
            "category to find",
            setOf(ImmutableAtcLevel.builder().code("category to find").name("").build()),
            EVALUATION_DATE.plusDays(1)
        )
    }
}