package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.junit.Test

class HasRecentlyReceivedMedicationOfAtcLevelTest {
    private val evaluationDate = TestClinicalFactory.createMinimalTestClinicalRecord().patient.registrationDate.plusWeeks(1)
    private val function = HasRecentlyReceivedMedicationOfAtcLevel(
        MedicationTestFactory.alwaysActive(),
        "category to find",
        setOf(AtcLevel(code = "category to find", name = "")),
        evaluationDate.plusDays(1),
        onlyCheckSystemic = false
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
            evaluationDate.minusDays(1),
            onlyCheckSystemic = false
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
            evaluationDate.minusWeeks(2),
            onlyCheckSystemic = false
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(MedicationTestFactory.medication(atc = atc, stopDate = evaluationDate))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    @Test
    fun `Should only consider systemic drugs when isSystemic is true`() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysActive(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            evaluationDate.plusDays(1),
            onlyCheckSystemic = true
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val systemicDrug = listOf(
            MedicationTestFactory.medication(
                atc = atc, administrationRoute = MedicationSelector.SYSTEMIC_ADMINISTRATION_ROUTE_SET.iterator().next()
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(systemicDrug)))

        val nonSystemicDrug = listOf(
            MedicationTestFactory.medication(
                atc = atc, administrationRoute = "non-systemic"
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(nonSystemicDrug)))
    }
}