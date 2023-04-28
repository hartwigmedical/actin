package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDate

class HasHistoryOfPneumonitisTest {
    @Test
    fun canEvaluateOnPriorOtherConditions() {
        val function = createTestPneumonitisFunction()

        // Test empty doid
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with wrong doid
        conditions.add(OtherConditionTestFactory.builder().addDoids("wrong doid").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Add a condition with correct DOID
        val pneumonitisDoid = DoidConstants.PNEUMONITIS_DOID
        conditions.add(OtherConditionTestFactory.builder().addDoids(pneumonitisDoid).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun canEvaluateOnToxicities() {
        val function = createTestPneumonitisFunction()

        // Test no toxicities
        val toxicities: MutableList<Toxicity> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add an irrelevant toxicity
        toxicities.add(toxicityBuilder().name("Not a relevant one").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
        val relevantToxicity: String = HasHistoryOfPneumonitis.TOXICITIES_CAUSING_PNEUMONITIS.iterator().next()
        // Add a toxicity with too low grade
        toxicities.add(toxicityBuilder().name(relevantToxicity).source(ToxicitySource.EHR).grade(1).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))

        // Add a valid toxicity
        toxicities.add(toxicityBuilder().name(relevantToxicity).source(ToxicitySource.EHR).grade(3).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)))
    }

    companion object {
        fun toxicityBuilder(): ImmutableToxicity.Builder {
            return ImmutableToxicity.builder().name(Strings.EMPTY).evaluatedDate(LocalDate.of(2010, 1, 1))
                .source(ToxicitySource.QUESTIONNAIRE)
        }

        private fun createTestPneumonitisFunction(): HasHistoryOfPneumonitis {
            return HasHistoryOfPneumonitis(TestDoidModelFactory.createMinimalTestDoidModel())
        }
    }
}