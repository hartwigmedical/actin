package com.hartwig.actin.treatment.input.composite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.junit.Test;

public class CompositeRulesTest {

    @Test
    public void canInterpretCompositeRules() {
        EligibilityRule firstCompositeRule = CompositeRules.COMPOSITE_RULE_MAPPING.keySet().iterator().next();
        assertTrue(CompositeRules.isComposite(firstCompositeRule));
        assertNotNull(CompositeRules.inputsForCompositeRule(firstCompositeRule));

        assertFalse(CompositeRules.isComposite(EligibilityRule.HAS_ACTIVE_INFECTION));
    }
}