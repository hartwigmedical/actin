package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibilityFactoryTest {

    @Test
    fun canDetermineWhetherRuleIsValid() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()

        // Simple rules
        assertThat(factory.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1]")).isTrue
        assertThat(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])")).isTrue

        // Trailing whitespace is allowed
        assertThat(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])  ")).isTrue

        // Complex rule with multiple AND & OR.
        assertThat(
            factory.isValidInclusionCriterion(
                "OR(AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1], HAS_PT_ULN_OF_AT_MOST_X[2]), "
                        + "HAS_APTT_ULN_OF_AT_MOST_X[3]), HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING)"
            )
        ).isTrue

        // Rules with composite functions with more than 2 inputs.
        assertThat(
            factory.isValidInclusionCriterion(
                "OR(AND(HAS_LIVER_METASTASES, HAS_ASAT_ULN_OF_AT_MOST_X[5], HAS_ALAT_ULN_OF_AT_MOST_X[5]), "
                        + "AND(HAS_ASAT_ULN_OF_AT_MOST_X[2.5], HAS_ALAT_ULN_OF_AT_MOST_X[2.5]))"
            )
        ).isTrue

        // Generally wrong:
        assertThat(factory.isValidInclusionCriterion("This is not a valid criterion")).isFalse

        // Wrong number of parameters:
        assertThat(factory.isValidInclusionCriterion("AND(IS_PREGNANT)")).isFalse
        assertThat(factory.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1.5, 2.5]")).isFalse

        // Should not have trailing stuff.
        assertThat(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1]) this should not be here")).isFalse

        // IS_PREGNANT is not a composite function
        assertThat(factory.isValidInclusionCriterion("IS_PREGNANT(HAS_INR_ULN_OF_AT_MOST_X[1])")).isFalse

        // Missing bracket "]"
        assertThat(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1)")).isFalse

        // Missing parenthesis ")"
        assertThat(factory.isValidInclusionCriterion("NOT(IS_PREGNANT")).isFalse
    }

    @Test
    fun canGenerateSimpleEligibilityFunction() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()
        val function = factory.generateEligibilityFunction("HAS_INR_ULN_OF_AT_MOST_X[1]")
        assertThat(function.rule).isEqualTo(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        assertThat(function.parameters).hasSize(1)
        assertThat(function.parameters).containsExactly("1")

        val notFunction = factory.generateEligibilityFunction("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])")
        assertThat(notFunction.rule).isEqualTo(EligibilityRule.NOT)
        assertThat(notFunction.parameters).hasSize(1)

        val subFunction = find(notFunction.parameters, EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        assertThat(subFunction).isEqualTo(function)
    }

    @Test
    fun canGenerateComplexCompositeEligibilityFunction() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()
        val criterion = "OR(IS_PREGNANT, AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1.5], HAS_PT_ULN_OF_AT_MOST_X[2]), HAS_APTT_ULN_OF_AT_MOST_X[3]))"
        val orRoot = factory.generateEligibilityFunction(criterion)
        assertThat(orRoot.rule).isEqualTo(EligibilityRule.OR)
        assertThat(orRoot.parameters).hasSize(2)

        val orRootInput1 = find(orRoot.parameters, EligibilityRule.IS_PREGNANT)
        assertThat(orRootInput1.parameters).hasSize(0)

        val orRootInput2 = find(orRoot.parameters, EligibilityRule.AND)
        assertThat(orRootInput2.parameters).hasSize(2)

        val andInput1 = find(orRootInput2.parameters, EligibilityRule.OR)
        assertThat(andInput1.parameters).hasSize(2)

        val andInput2 = find(orRootInput2.parameters, EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X)
        assertThat(andInput2.parameters).hasSize(1)
        assertThat(andInput2.parameters).containsExactly("3")

        val secondOrInput1 = find(andInput1.parameters, EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        assertThat(secondOrInput1.parameters).hasSize(1)
        assertThat(secondOrInput1.parameters).containsExactly("1.5")

        val secondOrInput2 = find(andInput1.parameters, EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X)
        assertThat(secondOrInput2.parameters).hasSize(1)
        assertThat(secondOrInput2.parameters).containsExactly("2")
    }

    companion object {
        private fun find(functions: List<Any>, rule: EligibilityRule): EligibilityFunction {
            for (function in functions) {
                val eligibilityFunction = function as EligibilityFunction
                if (eligibilityFunction.rule == rule) {
                    return eligibilityFunction
                }
            }
            throw IllegalStateException("Could not find eligibility function with rule $rule")
        }
    }
}