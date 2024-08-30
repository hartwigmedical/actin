package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasChildPughClassTest {
    private val alcoholicLiverCirrhosis = "14018"
    private val function = HasChildPughClass(
        TestDoidModelFactory.createWithOneParentChild(DoidConstants.LIVER_CIRRHOSIS_DOID, alcoholicLiverCirrhosis)
    )

    @Test
    fun `Should not evaluate when liver cirrhosis not present`() {
        val conditions = listOf(
            priorOtherCondition(doids = setOf(DoidConstants.HEART_DISEASE_DOID)),
            priorOtherCondition(doids = setOf(DoidConstants.LUNG_DISEASE_DOID))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should evaluate undetermined when liver cirrhosis present`() {
        val conditions = listOf(
            priorOtherCondition(doids = setOf(DoidConstants.LUNG_DISEASE_DOID)),
            priorOtherCondition(doids = setOf(alcoholicLiverCirrhosis))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withPriorOtherConditions(conditions)))
    }
}