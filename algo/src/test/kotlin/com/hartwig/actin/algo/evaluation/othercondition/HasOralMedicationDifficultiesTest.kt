package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import org.junit.Test

class HasOralMedicationDifficultiesTest {
    @Test
    fun canEvaluate() {
        val function = HasOralMedicationDifficulties()

        // Test no complications
        val complications: MutableList<Complication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a random complication
        complications.add(ImmutableComplication.builder().name("not a problem").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a real shallow difficulty one
        val oralMedicationComplication: String =
            HasOralMedicationDifficulties.COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES.iterator().next()
        complications.add(ImmutableComplication.builder().name(oralMedicationComplication).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }
}