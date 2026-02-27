package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

class HasHadRecentResectionTest {

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassOnRecentResection() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, startYear = 2022, startMonth = 11)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldWarnForResectionCloseToMinDate() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENT_SET, startYear = 2022, startMonth = 10)
        assertEvaluation(EvaluationResult.WARN, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForResectionWithUnknownDate() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(MATCHING_TREATMENT_SET)))
        )
    }

    @Test
    fun shouldReturnUndeterminedForRecentUnspecifiedSurgery() {
        val treatments = setOf(treatment("", false, categories = setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(
                withTreatmentHistoryEntry(treatmentHistoryEntry(treatments, startYear = 2022, startMonth = 11))
            )
        )
    }

    companion object {
        private val MIN_DATE = LocalDate.of(2022, 10, 12)
        private val FUNCTION = HasHadRecentResection(MIN_DATE)
        private val MATCHING_TREATMENT_SET = setOf(treatment("some form of " + HasHadRecentResection.RESECTION_KEYWORD, false))
    }
}