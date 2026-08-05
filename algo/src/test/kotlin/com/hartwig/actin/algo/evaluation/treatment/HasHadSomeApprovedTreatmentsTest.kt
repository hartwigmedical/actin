package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import org.junit.jupiter.api.Test

class HasHadSomeApprovedTreatmentsTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).treatment

    @Test
    fun canEvaluate() {
        val function = HasHadSomeApprovedTreatments(1, labels)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))

        val record = TreatmentTestFactory.withTreatmentHistoryEntry(TreatmentTestFactory.treatmentHistoryEntry())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }
}