package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

class HasHadAnyCancerTreatmentSinceDateTest {

    private val monthsAgo = 6
    private val minDate = LocalDate.of(2024, 2, 9).minusMonths(monthsAgo.toLong())
    private val recentDate = minDate.plusMonths(3)
    private val olderDate = minDate.minusMonths(3)
    private val atcLevels = AtcLevel(code = "L01", name = "")
    private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
    private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)
    val function = HasHadAnyCancerTreatmentSinceDate(minDate, monthsAgo, setOf(atcLevels), INTERPRETER)
    val chemotherapyTreatment = TreatmentTestFactory.treatment(
        name = "Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
    )
    val immunotherapyTreatment = TreatmentTestFactory.treatment(
        name = "Immunotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
    )

    @Test
    fun `Should fail when oncological history is empty`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(emptyList())
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(priorCancerTreatment))
    }

    @Test
    fun `Should fail when all prior treatment is stopped before the minimal allowed date`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(chemotherapyTreatment),
                    stopYear = olderDate.year,
                    stopMonth = olderDate.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(priorCancerTreatment))
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
    fun `Should pass when some prior treatment is given after the minimal allowed date`() {
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
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(priorCancerTreatment))
        Assertions.assertThat(function.evaluate(priorCancerTreatment).passGeneralMessages).containsExactly(
            "Received anti-cancer therapy (Immunotherapy) within the last $monthsAgo months"
        )
    }
}