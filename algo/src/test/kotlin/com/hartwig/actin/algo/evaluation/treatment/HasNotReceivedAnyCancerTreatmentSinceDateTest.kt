package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

class HasNotReceivedAnyCancerTreatmentSinceDateTest {

    private val monthsAgo = 6
    private val minDate = LocalDate.of(2024, 2, 9).minusMonths(monthsAgo.toLong())
    private val recentDate = minDate.plusMonths(3)
    private val olderDate = minDate.minusMonths(3)
    val function = HasNotReceivedAnyCancerTreatmentSinceDate(minDate, monthsAgo)
    val chemotherapyTreatment = TreatmentTestFactory.treatment(
        name = "Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
    )
    val immunotherapyTreatment = TreatmentTestFactory.treatment(
        name = "Immunotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
    )

    @Test
    fun `Should pass when oncological history is empty `() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(emptyList())
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(priorCancerTreatment))
    }

    @Test
    fun `Should pass when all prior treatment is stopped before the minimal allowed date`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(chemotherapyTreatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(priorCancerTreatment))
    }

    @Test
    fun `Should warn if any prior anti cancer therapy is stopped less then or equal to 1 month after the minimal allowed date`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(chemotherapyTreatment),
                    stopYear = minDate.year,
                    stopMonth = minDate.plusMonths(1).monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(priorCancerTreatment))
    }

    @Test
    fun `Should evaluate to undetermined if the stop date is unknown for any prior anti cancer therapy`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(chemotherapyTreatment),
                    stopYear = null,
                    stopMonth = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(immunotherapyTreatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(priorCancerTreatment))
        Assertions.assertThat(function.evaluate(priorCancerTreatment).undeterminedGeneralMessages).containsExactly(
            "Received anti-cancer therapy (Chemotherapy) but undetermined if in the last $monthsAgo months (date unknown)"
        )
    }

    @Test
    fun `Should fail when some prior treatment is given after the minimal allowed date`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(chemotherapyTreatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(immunotherapyTreatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(immunotherapyTreatment),
                    stopYear = recentDate.year,
                    stopMonth = recentDate.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(priorCancerTreatment))
        Assertions.assertThat(function.evaluate(priorCancerTreatment).failGeneralMessages).containsExactly(
            "Received anti-cancer therapy (Immunotherapy) within the last $monthsAgo months"
        )
    }
}