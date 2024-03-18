package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.complication
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.toxicity
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasPotentialAbsorptionDifficultiesTest {
    val function = HasPotentialAbsorptionDifficulties(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun canEvaluateOnPriorOtherConditions() {
        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with wrong doid
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf("wrong doid")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with correct DOID
        val absorptionDoid = DoidConstants.ABSORPTION_DIFFICULTIES_DOID_SET.iterator().next()
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf(absorptionDoid)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun canEvaluateOnComplications() {
        // Test no complications
        val complications: MutableList<Complication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a random complication
        complications.add(complication(name = "not a problem", categories = setOf("random complication")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)))

        // Add a real absorption one
        complications.add(
            complication(
                name = "real complication", categories = setOf(HasPotentialAbsorptionDifficulties.GASTROINTESTINAL_DISORDER_CATEGORY)
            )
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)))
    }

    @Test
    fun canEvaluateOnToxicities() {
        // Test no toxicities
        val toxicities: MutableList<Toxicity> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add an irrelevant toxicity
        toxicities.add(toxicity("Not a relevant one", ToxicitySource.QUESTIONNAIRE, null))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
        val relevantToxicityName = HasPotentialAbsorptionDifficulties.TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY.iterator().next()
        // Add a toxicity with too low grade
        toxicities.add(toxicity(relevantToxicityName, ToxicitySource.EHR, 1))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add a valid toxicity
        toxicities.add(toxicity(relevantToxicityName, ToxicitySource.EHR, 3))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
    }
}