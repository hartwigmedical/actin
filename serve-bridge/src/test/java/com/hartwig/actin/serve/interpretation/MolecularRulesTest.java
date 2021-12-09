package com.hartwig.actin.serve.interpretation;

import static org.junit.Assert.assertFalse;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;

import org.junit.Test;

public class MolecularRulesTest {

    @Test
    public void noMolecularRuleIsAlsoComposite() {
        for (EligibilityRule rule : EligibilityRule.values()) {
            assertFalse(MolecularRules.isMolecular(rule) && CompositeRules.isComposite(rule));
        }
    }
}