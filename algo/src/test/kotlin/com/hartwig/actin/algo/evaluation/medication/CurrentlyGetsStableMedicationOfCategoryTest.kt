package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsStableMedicationOfCategoryTest {
    @Test
    fun canEvaluateOnOneTerm() {
        val category1 = "category 1"
        val function = CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(category1))

        // Fails on no medication
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes with single medication with dosing.
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(category1).build()).build()
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes with another medication with no category and same dosing
        val noAtc = AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("").build()).build()
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(noAtc).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Fails on same category and other dosing.
        medications.add(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(atc).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Also fail in case a dosing is combined with medication without dosing.
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder()
                            .dosage(fixedDosing())
                            .atc(atc)
                            .build(), TestMedicationFactory.builder().atc(atc).build()
                    )
                )
            )
        )
    }

    @Test
    fun canEvaluateForMultipleTerms() {
        val category1 = "category 1"
        val category2 = "category 2"
        val function = CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(category1, category2))

        // Passes with single medication with dosing.
        val medications: MutableList<Medication> = mutableListOf()
        val atc1 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(category1).build()).build()
        val atc2 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(category2).build()).build()
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc1).build())
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc2).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes on same category and other dosing.
        medications.add(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(atc1).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Start failing when both categories have wrong dosing.
        medications.add(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(atc2).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    companion object {
        private fun fixedDosing(): Dosage {
            return ImmutableDosage.builder()
                .dosageMin(1.0)
                .dosageMax(2.0)
                .dosageUnit("unit 1")
                .frequency(3.0)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build()
        }
    }
}