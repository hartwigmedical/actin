package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import org.junit.Assert
import org.junit.Test

class EligibilityFactoryTest {
    @Test
    fun canDetermineWhetherRuleIsValid() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()

        // Simple rules
        Assert.assertTrue(factory.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1]"))
        Assert.assertTrue(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])"))

        // Trailing whitespace is allowed
        Assert.assertTrue(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])  "))

        // Complex rule with multiple AND & OR.
        Assert.assertTrue(
            factory.isValidInclusionCriterion(
                "OR(AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1], HAS_PT_ULN_OF_AT_MOST_X[2]), "
                        + "HAS_APTT_ULN_OF_AT_MOST_X[3]), HAS_STABLE_ANTICOAGULANT_MEDICATION_DOSING)"
            )
        )

        // Rules with composite functions with more than 2 inputs.
        Assert.assertTrue(
            factory.isValidInclusionCriterion(
                "OR(AND(HAS_LIVER_METASTASES, HAS_ASAT_ULN_OF_AT_MOST_X[5], HAS_ALAT_ULN_OF_AT_MOST_X[5]), "
                        + "AND(HAS_ASAT_ULN_OF_AT_MOST_X[2.5], HAS_ALAT_ULN_OF_AT_MOST_X[2.5]))"
            )
        )

        // Generally wrong:
        Assert.assertFalse(factory.isValidInclusionCriterion("This is not a valid criterion"))

        // Wrong number of parameters:
        Assert.assertFalse(factory.isValidInclusionCriterion("AND(IS_PREGNANT)"))
        Assert.assertFalse(factory.isValidInclusionCriterion("HAS_INR_ULN_OF_AT_MOST_X[1.5, 2.5]"))

        // Should not have trailing stuff.
        Assert.assertFalse(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1]) this should not be here"))

        // IS_PREGNANT is not a composite function
        Assert.assertFalse(factory.isValidInclusionCriterion("IS_PREGNANT(HAS_INR_ULN_OF_AT_MOST_X[1])"))

        // Missing bracket "]"
        Assert.assertFalse(factory.isValidInclusionCriterion("NOT(HAS_INR_ULN_OF_AT_MOST_X[1)"))

        // Missing parenthesis ")"
        Assert.assertFalse(factory.isValidInclusionCriterion("NOT(IS_PREGNANT"))
    }

    @Test
    fun canGenerateSimpleEligibilityFunction() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()
        val function = factory.generateEligibilityFunction("HAS_INR_ULN_OF_AT_MOST_X[1]")
        Assert.assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, function.rule())
        Assert.assertEquals(1, function.parameters().size.toLong())
        Assert.assertTrue(function.parameters().contains("1"))
        val notFunction = factory.generateEligibilityFunction("NOT(HAS_INR_ULN_OF_AT_MOST_X[1])")
        Assert.assertEquals(EligibilityRule.NOT, notFunction.rule())
        Assert.assertEquals(1, notFunction.parameters().size.toLong())
        val subFunction = find(notFunction.parameters(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assert.assertEquals(function, subFunction)
    }

    @Test
    fun canGenerateComplexCompositeEligibilityFunction() {
        val factory = TestEligibilityFactoryFactory.createTestEligibilityFactory()
        val criterion = "OR(IS_PREGNANT, AND(OR(HAS_INR_ULN_OF_AT_MOST_X[1.5], HAS_PT_ULN_OF_AT_MOST_X[2]), HAS_APTT_ULN_OF_AT_MOST_X[3]))"
        val orRoot = factory.generateEligibilityFunction(criterion)
        Assert.assertEquals(EligibilityRule.OR, orRoot.rule())
        Assert.assertEquals(2, orRoot.parameters().size.toLong())
        val orRootInput1 = find(orRoot.parameters(), EligibilityRule.IS_PREGNANT)
        Assert.assertEquals(0, orRootInput1.parameters().size.toLong())
        val orRootInput2 = find(orRoot.parameters(), EligibilityRule.AND)
        Assert.assertEquals(2, orRootInput2.parameters().size.toLong())
        val andInput1 = find(orRootInput2.parameters(), EligibilityRule.OR)
        Assert.assertEquals(2, andInput1.parameters().size.toLong())
        val andInput2 = find(orRootInput2.parameters(), EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X)
        Assert.assertEquals(1, andInput2.parameters().size.toLong())
        Assert.assertTrue(andInput2.parameters().contains("3"))
        val secondOrInput1 = find(andInput1.parameters(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        Assert.assertEquals(1, secondOrInput1.parameters().size.toLong())
        Assert.assertTrue(secondOrInput1.parameters().contains("1.5"))
        val secondOrInput2 = find(andInput1.parameters(), EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X)
        Assert.assertEquals(1, secondOrInput2.parameters().size.toLong())
        Assert.assertTrue(secondOrInput2.parameters().contains("2"))
    }

    companion object {
        private fun find(functions: List<Any>, rule: EligibilityRule): EligibilityFunction {
            for (function in functions) {
                val eligibilityFunction = function as EligibilityFunction
                if (eligibilityFunction.rule() == rule) {
                    return eligibilityFunction
                }
            }
            throw IllegalStateException("Could not find eligibility function with rule $rule")
        }
    }
}