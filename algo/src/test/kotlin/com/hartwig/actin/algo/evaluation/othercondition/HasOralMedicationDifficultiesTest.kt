package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Complication
import org.junit.Test

class HasOralMedicationDifficultiesTest {

    @Test
    fun canEvaluate() {
        val function = HasOralMedicationDifficulties()

        // Test no complications
        val complications: MutableList<Complication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a random complication
        complications.add(OtherConditionTestFactory.complication(name = "not a problem"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a real shallow difficulty one
        val oralMedicationComplication = HasOralMedicationDifficulties.COMPLICATIONS_CAUSING_SWALLOW_DIFFICULTIES.iterator().next()
        complications.add(OtherConditionTestFactory.complication(name = oralMedicationComplication))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }
}