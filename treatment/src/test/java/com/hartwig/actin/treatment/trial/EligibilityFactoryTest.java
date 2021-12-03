package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EligibilityFactoryTest {

    @Test
    public void canDetermineWhetherRuleIsValid() {
        // Simple rules
        assertTrue(EligibilityFactory.isValidInclusionCriterion("HAS_INR_ULN_AT_MOST_X[1]"));
        assertTrue(EligibilityFactory.isValidInclusionCriterion("NOT(HAS_INR_ULN_AT_MOST_X[1])"));

        // Trailing whitespace is allowed
        assertTrue(EligibilityFactory.isValidInclusionCriterion("NOT(HAS_INR_ULN_AT_MOST_X[1])  "));

        // Complex rule with multiple AND and OR.
        assertTrue(EligibilityFactory.isValidInclusionCriterion("OR(AND(OR(HAS_INR_ULN_AT_MOST_X[1], HAS_PT_ULN_AT_MOST_X[2]), "
                + " HAS_APTT_ULN_AT_MOST_X[3]), HAS_STABLE_ANTICOAGULANT_DOSING)"));

        // Rules with composite functions with more than 2 inputs.
        assertTrue(EligibilityFactory.isValidInclusionCriterion(
                "OR(AND(HAS_LIVER_METASTASES, HAS_ASAT_ULN_OF_AT_MOST_X[5], HAS_ALAT_ULN_OF_AT_MOST_X[5]), "
                        + "AND(HAS_ASAT_ULN_OF_AT_MOST_X[2.5], HAS_ALAT_ULN_OF_AT_MOST_X[2.5]))"));

        // Generally wrong:
        assertFalse(EligibilityFactory.isValidInclusionCriterion("This is not a valid criterion"));

        // Wrong number of parameters:
        assertFalse(EligibilityFactory.isValidInclusionCriterion("AND(IS_PREGNANT)"));
        assertFalse(EligibilityFactory.isValidInclusionCriterion("HAS_INR_ULN_AT_MOST_X[1.5, 2.5]"));

        // Should not have trailing stuff.
        assertFalse(EligibilityFactory.isValidInclusionCriterion("NOT(HAS_INR_ULN_AT_MOST_X[1]) this should not be here"));

        // IS_PREGNANT is not a composite function
        assertFalse(EligibilityFactory.isValidInclusionCriterion("IS_PREGNANT(HAS_INR_ULN_AT_MOST_X[1])"));

        // Missing bracket "]"
        assertFalse(EligibilityFactory.isValidInclusionCriterion("NOT(HAS_INR_ULN_AT_MOST_X[1)"));

        // Missing parenthesis ")"
        assertFalse(EligibilityFactory.isValidInclusionCriterion("NOT(IS_PREGNANT"));
    }

    @Test
    public void canGenerateSimpleEligibilityFunction() {
        EligibilityFunction function = EligibilityFactory.generateEligibilityFunction("HAS_INR_ULN_AT_MOST_X[1]");
        assertEquals(EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X, function.rule());
        assertEquals(1, function.parameters().size());
        assertTrue(function.parameters().contains("1"));

        EligibilityFunction notFunction = EligibilityFactory.generateEligibilityFunction("NOT(HAS_INR_ULN_AT_MOST_X[1])");
        assertEquals(EligibilityRule.NOT, notFunction.rule());
        assertEquals(1, notFunction.parameters().size());

        EligibilityFunction subFunction = find(notFunction.parameters(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X);
        assertEquals(function, subFunction);
    }

    @Test
    public void canGenerateComplexCompositeEligibilityFunction() {
        String criterion = "OR(IS_PREGNANT, AND(OR(HAS_INR_ULN_AT_MOST_X[1.5], HAS_PT_ULN_AT_MOST_X[2]), HAS_APTT_ULN_AT_MOST_X[3]))";

        EligibilityFunction orRoot = EligibilityFactory.generateEligibilityFunction(criterion);

        assertEquals(EligibilityRule.OR, orRoot.rule());
        assertEquals(2, orRoot.parameters().size());

        EligibilityFunction orRootInput1 = find(orRoot.parameters(), EligibilityRule.IS_PREGNANT);
        assertEquals(0, orRootInput1.parameters().size());

        EligibilityFunction orRootInput2 = find(orRoot.parameters(), EligibilityRule.AND);
        assertEquals(2, orRootInput2.parameters().size());

        EligibilityFunction andInput1 = find(orRootInput2.parameters(), EligibilityRule.OR);
        assertEquals(2, andInput1.parameters().size());

        EligibilityFunction andInput2 = find(orRootInput2.parameters(), EligibilityRule.HAS_APTT_ULN_OF_AT_MOST_X);
        assertEquals(1, andInput2.parameters().size());
        assertTrue(andInput2.parameters().contains("3"));

        EligibilityFunction secondOrInput1 = find(andInput1.parameters(), EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X);
        assertEquals(1, secondOrInput1.parameters().size());
        assertTrue(secondOrInput1.parameters().contains("1.5"));

        EligibilityFunction secondOrInput2 = find(andInput1.parameters(), EligibilityRule.HAS_PT_ULN_OF_AT_MOST_X);
        assertEquals(1, secondOrInput2.parameters().size());
        assertTrue(secondOrInput2.parameters().contains("2"));
    }

    @NotNull
    private static EligibilityFunction find(@NotNull List<Object> functions, @NotNull EligibilityRule rule) {
        for (Object function : functions) {
            EligibilityFunction eligibilityFunction = (EligibilityFunction) function;
            if (eligibilityFunction.rule() == rule) {
                return eligibilityFunction;
            }
        }

        throw new IllegalStateException("Could not find eligibility function with rule " + rule);
    }
}