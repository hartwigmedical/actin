package com.hartwig.actin

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.util.TestEligibilityUtility
import org.assertj.core.api.Assertions
import org.junit.Test

class EligibilityFactoryAndFunctionInputResolverTest {

    @Test
    fun canDetermineWhetherRuleIsValid() {

        // Simple rules
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1]")).isTrue
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])")).isTrue

        // Trailing whitespace is allowed
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])  ")).isTrue

        // Complex rule with multiple AND & OR.
        Assertions.assertThat(
            TestEligibilityUtility.isValidInclusionCriterion(
                "OR(AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1], HAS_PT_ULN_OF_AT_MOST_X[2]), "
                        + "HAS_APTT_ULN_OF_AT_MOST_X[3]), HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING)"
            )
        ).isTrue

        // Rules with composite functions with more than 2 inputs.
        Assertions.assertThat(
            TestEligibilityUtility.isValidInclusionCriterion(
                "OR(AND(HAS_LIVER_METASTASES, HAS_ASAT_ULN_OF_AT_MOST_X[5], HAS_ALAT_ULN_OF_AT_MOST_X[5]), "
                        + "AND(HAS_ASAT_ULN_OF_AT_MOST_X[2.5], HAS_ALAT_ULN_OF_AT_MOST_X[2.5]))"
            )
        ).isTrue

        // Generally wrong:
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("This is not a valid criterion")).isFalse

        // Wrong number of parameters:
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("AND(IS_PREGNANT)")).isFalse
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1.5, 2.5]")).isFalse

        // Should not have trailing stuff.
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1]) this should not be here")).isFalse

        // IS_PREGNANT is not a composite function
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("IS_PREGNANT(HAS_INR_ULN_OF_AT_MOST_X[1])")).isFalse

        // Missing bracket "]"
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1)")).isFalse

        // Missing parenthesis ")"
        Assertions.assertThat(TestEligibilityUtility.isValidInclusionCriterion("NOT(IS_PREGNANT")).isFalse
    }

    @Test
    fun canGenerateSimpleEligibilityFunction() {
        val function = EligibilityFactory.generateEligibilityFunction("HAS_INR_ULN_OF_AT_MOST_X[1]")
        Assertions.assertThat(function.rule).isEqualTo(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assertions.assertThat(function.parameters).hasSize(1)
        Assertions.assertThat(function.parameters).containsExactly("1")

        val notFunction = EligibilityFactory.generateEligibilityFunction("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])")
        Assertions.assertThat(notFunction.rule).isEqualTo(EligibilityRule.NOT)
        Assertions.assertThat(notFunction.parameters).hasSize(1)

        val subFunction = find(notFunction.parameters, EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assertions.assertThat(subFunction).isEqualTo(function)
    }

    @Test
    fun canGenerateComplexCompositeEligibilityFunction() {
        val criterion = "OR(IS_PREGNANT, AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1.5], HAS_PT_ULN_OF_AT_MOST_X[2]), HAS_APTT_ULN_OF_AT_MOST_X[3]))"
        val orRoot = EligibilityFactory.generateEligibilityFunction(criterion)
        Assertions.assertThat(orRoot.rule).isEqualTo(EligibilityRule.OR)
        Assertions.assertThat(orRoot.parameters).hasSize(2)

        val orRootInput1 = find(orRoot.parameters, EligibilityRule.IS_PREGNANT)
        Assertions.assertThat(orRootInput1.parameters).hasSize(0)

        val orRootInput2 = find(orRoot.parameters, EligibilityRule.AND)
        Assertions.assertThat(orRootInput2.parameters).hasSize(2)

        val andInput1 = find(orRootInput2.parameters, EligibilityRule.OR)
        Assertions.assertThat(andInput1.parameters).hasSize(2)

        val andInput2 = find(orRootInput2.parameters, EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X)
        Assertions.assertThat(andInput2.parameters).hasSize(1)
        Assertions.assertThat(andInput2.parameters).containsExactly("3")

        val secondOrInput1 = find(andInput1.parameters, EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assertions.assertThat(secondOrInput1.parameters).hasSize(1)
        Assertions.assertThat(secondOrInput1.parameters).containsExactly("1.5")

        val secondOrInput2 = find(andInput1.parameters, EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X)
        Assertions.assertThat(secondOrInput2.parameters).hasSize(1)
        Assertions.assertThat(secondOrInput2.parameters).containsExactly("2")
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