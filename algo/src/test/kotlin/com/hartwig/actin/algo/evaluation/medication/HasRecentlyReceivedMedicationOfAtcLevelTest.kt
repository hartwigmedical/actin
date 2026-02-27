package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasRecentlyReceivedMedicationOfAtcLevelTest {
    private val evaluationDate = TestClinicalFactory.createMinimalTestClinicalRecord().patient.registrationDate.plusWeeks(1)
    private val function = HasRecentlyReceivedMedicationOfAtcLevel(
        MedicationTestFactory.alwaysActive(),
        "category to find",
        setOf(AtcLevel(code = "category to find", name = "")),
        evaluationDate.plusDays(1)
    )
    
    @Test
    fun `Should fail when no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail when medication has wrong category`() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(MedicationTestFactory.medication(atc = atc))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has right category`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(MedicationTestFactory.medication(atc = atc))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has correct date`() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            evaluationDate.minusDays(1)
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(MedicationTestFactory.medication(atc = atc, stopDate = evaluationDate))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should be undetermined when medication stopped after min stop date`() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            evaluationDate.minusWeeks(2)
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(MedicationTestFactory.medication(atc = atc, stopDate = evaluationDate))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.recoverable).isTrue()
    }
}