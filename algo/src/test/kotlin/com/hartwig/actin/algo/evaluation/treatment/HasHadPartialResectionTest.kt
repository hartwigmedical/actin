package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatment
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.algo.evaluation.treatment.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadPartialResectionTest {

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldPassOnPartialResection() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment(HasHadPartialResection.PARTIAL_RESECTION, false)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun shouldReturnUndeterminedForUnspecifiedResection() {
        val treatments = setOf(treatment("some form of " + HasHadPartialResection.RESECTION_KEYWORD, false))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))))
    }

    @Test
    fun shouldReturnUndeterminedForUnspecifiedSurgery() {
        val treatments = setOf(treatment("", false, categories = setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))))
    }

    companion object {
        private val FUNCTION = HasHadPartialResection()
    }
}