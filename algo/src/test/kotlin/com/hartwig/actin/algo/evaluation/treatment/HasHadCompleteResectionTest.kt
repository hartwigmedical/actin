package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadCompleteResectionTest {

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassOnCompleteResection() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment(HasHadCompleteResection.COMPLETE_RESECTION, false)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForUnspecifiedResection() {
        val treatments = setOf(treatment("some form of " + HasHadCompleteResection.RESECTION_KEYWORD, false))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))))
    }

    @Test
    fun shouldReturnUndeterminedForUnspecifiedSurgery() {
        val treatments = setOf(treatment("", false, categories = setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))))
    }

    companion object {
        private val FUNCTION = HasHadCompleteResection()
    }
}