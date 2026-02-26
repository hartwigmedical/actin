package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val MONTHS_AGO = 6
private val MIN_DATE = LocalDate.of(2024, 2, 9)
private val RECENT_DATE = MIN_DATE.plusMonths(3)
private val OLDER_DATE = MIN_DATE.minusMonths(3)
private val ATC_LEVELS = AtcLevel(code = "category to find", name = "")
private val CATEGORY_TO_IGNORE = TreatmentCategory.TARGETED_THERAPY
private val TYPE_TO_IGNORE = setOf(DrugType.ALK_INHIBITOR)
private val TYPE_NOT_TO_IGNORE = setOf(DrugType.ROS1_INHIBITOR)
private val CHEMOTHERAPY_TREATMENT = TreatmentTestFactory.drugTreatment(
    name = "Chemotherapy", category = TreatmentCategory.CHEMOTHERAPY, types = setOf(DrugType.ALKYLATING_AGENT)
)
private val IMMUNOTHERAPY_TREATMENT = TreatmentTestFactory.drugTreatment(
    name = "Immunotherapy", category = TreatmentCategory.IMMUNOTHERAPY, types = setOf(DrugType.PD_1_PD_L1_ANTIBODY)
)

class HasHadAnyCancerTreatmentSinceDateTest {

    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function = HasHadAnyCancerTreatmentSinceDate(
        MIN_DATE,
        MONTHS_AGO,
        setOf(ATC_LEVELS),
        interpreter,
        CATEGORY_TO_IGNORE,
        TYPE_TO_IGNORE,
        false
    )
    private val functionOnlySystemic =
        HasHadAnyCancerTreatmentSinceDate(
            MIN_DATE,
            MONTHS_AGO,
            setOf(ATC_LEVELS),
            interpreter,
            CATEGORY_TO_IGNORE,
            TYPE_TO_IGNORE,
            true
        )

    @Test
    fun `Should fail when oncological history is empty`() {
        evaluateFunctions(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(emptyList()))
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
        evaluateFunctions(EvaluationResult.FAIL, priorCancerTreatment)
    }

    @Test
    fun `Should fail for non-systemic treatment given after the minimal allowed date when function is evaluating systemic treatments only`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(
                        TreatmentTestFactory.treatment(
                            name = "Immunotherapy", isSystemic = false, categories = setOf(TreatmentCategory.IMMUNOTHERAPY)
                        )
                    ),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        val evaluation = functionOnlySystemic.evaluate(priorCancerTreatment)
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly(
            "Has not received systemic anti-cancer therapy within $MONTHS_AGO months ignoring ${
                TYPE_TO_IGNORE.first().display()
            }"
        )
    }

    @Test
    fun `Should fail when some prior treatment is given after the minimal allowed date but is ignored type`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(
                        TreatmentTestFactory.drugTreatment(
                            name = "to ignore", category = CATEGORY_TO_IGNORE, types = TYPE_TO_IGNORE
                        )
                    ),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        evaluateFunctions(EvaluationResult.FAIL, priorCancerTreatment)
    }

    @Test
    fun `Should fail when some prior treatment is given after the minimal allowed date but has non-ignore and ignored type`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(
                        TreatmentTestFactory.drugTreatment(
                            name = "to ignore and non-ignore",
                            category = CATEGORY_TO_IGNORE,
                            types = TYPE_TO_IGNORE + TYPE_NOT_TO_IGNORE
                        )
                    ),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        evaluateFunctions(EvaluationResult.FAIL, priorCancerTreatment)
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
        evaluateFunctions(EvaluationResult.UNDETERMINED, priorCancerTreatment)
        assertThat(function.evaluate(priorCancerTreatment).undeterminedMessagesStrings()).containsExactly(
            "Received anti-cancer therapy but undetermined if in the last $MONTHS_AGO months (date unknown)"
        )
    }

    @Test
    fun `Should evaluate to undetermined when medication entry with trial medication`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true))
        evaluateFunctions(EvaluationResult.UNDETERMINED, WashoutTestFactory.withMedications(medications))
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
        evaluateFunctions(EvaluationResult.PASS, priorCancerTreatment)
        assertThat(function.evaluate(priorCancerTreatment).passMessagesStrings()).containsExactly(
            "Received anti-cancer therapy within the last $MONTHS_AGO months"
        )
    }

    @Test
    fun `Should pass when some prior treatment is given after the minimal allowed date with category to ignore but not type to ignore`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(
                        TreatmentTestFactory.drugTreatment(
                            name = "not to ignore", category = CATEGORY_TO_IGNORE, types = TYPE_NOT_TO_IGNORE
                        )
                    ),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        evaluateFunctions(EvaluationResult.PASS, priorCancerTreatment)
    }

    @Test
    fun `Should pass when some prior treatment is given after the minimal allowed date with one treatment with type to ignore and another treatment with a type not to ignore`() {
        val priorCancerTreatment = TreatmentTestFactory.withTreatmentHistory(
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = listOf(
                        TreatmentTestFactory.drugTreatment(
                            name = "to ignore", category = CATEGORY_TO_IGNORE, types = TYPE_TO_IGNORE
                        ),
                        TreatmentTestFactory.drugTreatment(
                            name = "not to ignore", category = CATEGORY_TO_IGNORE, types = TYPE_NOT_TO_IGNORE
                        )
                    ),
                    stopYear = RECENT_DATE.year,
                    stopMonth = RECENT_DATE.monthValue
                )
            )
        )
        evaluateFunctions(EvaluationResult.PASS, priorCancerTreatment)
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
        evaluateFunctions(EvaluationResult.PASS, priorCancerTreatment)
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(expected, function.evaluate(record))
        EvaluationAssert.assertEvaluation(expected, functionOnlySystemic.evaluate(record))
    }
}