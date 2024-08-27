package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

private val MIN_DATE = LocalDate.of(2020, 6, 6)

class HasRecentlyReceivedCancerTherapyOfNameTest {


    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function = HasRecentlyReceivedCancerTherapyOfName(setOf(Drug(name = "correct", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = emptySet())), interpreter, MIN_DATE)

    @Test
    fun `Should fail no medications`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail on medication with wrong name`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "other",
                            stopDate = MIN_DATE.plusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail on medication with old date`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "correct",
                            stopDate = MIN_DATE.minusDays(1)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass on medication with recent date`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                WashoutTestFactory.withMedications(
                    listOf(
                        WashoutTestFactory.medication(
                            name = "correct",
                            stopDate = MIN_DATE.plusDays(1)
                        )
                    )
                )
            )
        )
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