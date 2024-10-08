package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate

private const val MONTHS_AGO = 6
private val MIN_DATE = LocalDate.of(2024, 2, 9)
private val RECENT_DATE = MIN_DATE.plusMonths(3)
private val OLDER_DATE = MIN_DATE.minusMonths(3)
private val ATC_LEVELS = AtcLevel(code = "category to find", name = "")
val CHEMOTHERAPY_TREATMENT = TreatmentTestFactory.treatment(
    name = "Chemotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
)
val IMMUNOTHERAPY_TREATMENT = TreatmentTestFactory.treatment(
    name = "Immunotherapy", isSystemic = true, categories = setOf(TreatmentCategory.CHEMOTHERAPY)
)

class HasHadAnyCancerTreatmentSinceDateTest {

    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function = HasHadAnyCancerTreatmentSinceDate(MIN_DATE, MONTHS_AGO, setOf(ATC_LEVELS), interpreter)

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
            "Received anti-cancer therapy but undetermined if in the last $MONTHS_AGO months (date unknown)"
        )
    }

    @Test
    fun `Should evaluate to undetermined when medication entry with trial medication`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(WashoutTestFactory.withMedications(medications))
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
            "Received anti-cancer therapy within the last $MONTHS_AGO months"
        )
    }

    @Test
    fun `Should pass when all prior treatment is stopped before the minimal allowed date but some medication is given after the minimal allowed date`() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentsAndMedications(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(CHEMOTHERAPY_TREATMENT),
                    stopYear = OLDER_DATE.year,
                    stopMonth = OLDER_DATE.monthValue
                )
            ), listOf(WashoutTestFactory.medication(atc, MIN_DATE.plusMonths(1)))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(priorCancerTreatment))
    }

}