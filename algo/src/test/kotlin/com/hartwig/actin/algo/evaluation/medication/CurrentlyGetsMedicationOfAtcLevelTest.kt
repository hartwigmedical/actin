package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfAtcLevelTest {

    private val alwaysActiveFunction =
        CurrentlyGetsMedicationOfAtcLevel(
            MedicationTestFactory.alwaysActive(),
            "L01A",
            setOf(ImmutableAtcLevel.builder().code("L01A").name("").build())
        )

    private val alwaysPlannedFunction =
        CurrentlyGetsMedicationOfAtcLevel(
            MedicationTestFactory.alwaysPlanned(),
            "L01A",
            setOf(ImmutableAtcLevel.builder().code("L01A").name("").build())
        )

    @Test
    fun `Should fail when no medication`() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication has wrong category`() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has right category`() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("L01A").build())
                .build()
        assertEvaluation(
            EvaluationResult.PASS, alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder().atc(atc).build()
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when patient plans to use medication of right category`() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("L01A").build())
                .build()
        assertEvaluation(
            EvaluationResult.WARN, alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder().atc(atc).build()
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when patient plans to use medication with wrong category`() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

}