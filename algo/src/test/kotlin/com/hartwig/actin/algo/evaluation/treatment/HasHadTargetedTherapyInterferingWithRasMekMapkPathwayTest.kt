package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test

class HasHadTargetedTherapyInterferingWithRasMekMapkPathwayTest {

    private val function = HasHadTargetedTherapyInterferingWithRasMekMapkPathway()

    @Test
    fun `Should fail for no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should resolve to undetermined for correct treatment category`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TARGETED_THERAPY)))
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Has had targeted therapy (Test) but undetermined interference with RAS/MEK/MAPK pathway"
        )
    }
}