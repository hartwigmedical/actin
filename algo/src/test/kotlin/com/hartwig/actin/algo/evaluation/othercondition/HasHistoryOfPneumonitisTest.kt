package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.toxicity
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHistoryOfPneumonitisTest {
    private val function = HasHistoryOfPneumonitis(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun canEvaluateOnPriorOtherConditions() {
        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with wrong doid
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf("wrong doid")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with correct DOID
        conditions.add(OtherConditionTestFactory.priorOtherCondition(doids = setOf(DoidConstants.PNEUMONITIS_DOID)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun canEvaluateOnToxicities() {
        // Test no toxicities
        val toxicities: MutableList<Toxicity> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add an irrelevant toxicity
        toxicities.add(toxicity("Not a relevant one", ToxicitySource.QUESTIONNAIRE, null))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
        val relevantToxicityName = HasHistoryOfPneumonitis.TOXICITIES_CAUSING_PNEUMONITIS.iterator().next()
        // Add a toxicity with too low grade
        toxicities.add(toxicity(relevantToxicityName, ToxicitySource.EHR, 1))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add a valid toxicity
        toxicities.add(toxicity(relevantToxicityName, ToxicitySource.EHR, 3))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
    }
}