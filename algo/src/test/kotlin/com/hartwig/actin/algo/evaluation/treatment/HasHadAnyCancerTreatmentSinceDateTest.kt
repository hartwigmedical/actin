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

private const val MONTHS_AGO = 6
private val MIN_DATE = LocalDate.of(2024, 2, 9).minusMonths(MONTHS_AGO.toLong())
private val RECENT_DATE = MIN_DATE.plusMonths(3)
private val OLDER_DATE = MIN_DATE.minusMonths(3)
private val ATC_LEVELS = AtcLevel(code = "L01", name = "")
private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)
val CHEMOTHERAPY_TREATMENT = TreatmentTestFactory.treatment(
    name = "Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
)
val IMMUNOTHERAPY_TREATMENT = TreatmentTestFactory.treatment(
    name = "Immunotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
)

class HasHadAnyCancerTreatmentSinceDateTest {

    private val function = HasHadAnyCancerTreatmentSinceDate(MIN_DATE, MONTHS_AGO, setOf(ATC_LEVELS), INTERPRETER)

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
                    treatments = listOf(CHEMOTHERAPY_TREATMENT),
                    stopYear = OLDER_DATE.year,
                    stopMonth = OLDER_DATE.monthValue
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
                    treatments = listOf(CHEMOTHERAPY_TREATMENT),
                    stopYear = null,
                    stopMonth = null
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(IMMUNOTHERAPY_TREATMENT),
                    stopYear = OLDER_DATE.year,
                    stopMonth = OLDER_DATE.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(priorCancerTreatment))
        Assertions.assertThat(function.evaluate(priorCancerTreatment).undeterminedGeneralMessages).containsExactly(
            "Received anti-cancer therapy (Chemotherapy) but undetermined if in the last $MONTHS_AGO months (date unknown)"
        )
    }

    @Test
    fun `Should pass when some prior treatment is given after the minimal allowed date`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(CHEMOTHERAPY_TREATMENT),
                    stopYear = OLDER_DATE.year,
                    stopMonth = OLDER_DATE.monthValue
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(IMMUNOTHERAPY_TREATMENT),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                ),
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(IMMUNOTHERAPY_TREATMENT),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(priorCancerTreatment))
        Assertions.assertThat(function.evaluate(priorCancerTreatment).passGeneralMessages).containsExactly(
            "Received anti-cancer therapy (Immunotherapy) within the last $MONTHS_AGO months"
        )
    }
}