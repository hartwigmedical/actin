package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)

class HasRecentlyReceivedCancerTherapyOfCategoryTest {

    private val function = HasRecentlyReceivedCancerTherapyOfCategory(
        mapOf("category to find" to setOf(AtcLevel(code = "category to find", name = ""))),
        mapOf("categories to ignore" to setOf(AtcLevel(code = "category to ignore", name = ""))),
        INTERPRETER
    )

    @Test
    fun `Should fail when no medications`() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication has the wrong category`() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when medication has right category and old date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)))

    }

    @Test
    fun `Should pass when medication has right category and recent date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication is trial medication`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true, stopDate = REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        Assertions.assertThat(result.recoverable).isTrue()
    }
}