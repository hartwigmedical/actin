package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadPartialResectionTest {

    private val function = HasHadPartialResection()

    @Test
    fun `Should fail with no treatment history`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(emptyList())),
            "No partial resection in provided treatments"
        )
    }

    @Test
    fun `Should pass with partial resection`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment(PARTIAL_RESECTION, false)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "Partial resection in provided treatments"
        )
    }

    @Test
    fun `Should be undetermined for unspecified resection`() {
        val treatments = setOf(treatment("some form of " + RESECTION_KEYWORDS.last() + " surgery", false))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))),
            "Undetermined if resection in provided treatments was a partial resection"
        )
    }

    @Test
    fun `Should be undetermined for unspecified surgery`() {
        val treatments = setOf(treatment("SURGERY", false, categories = setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry(treatments))),
            "Undetermined if resection in provided treatments was a partial resection"
        )
    }
}