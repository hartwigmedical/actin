package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.CompositeRules;

import org.junit.Test;

public class FunctionCreatorFactoryTest {

    @Test
    public void everyFunctionIsMapped() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        Map<EligibilityRule, FunctionCreator> map = FunctionCreatorFactory.createFunctionCreatorMap(doidModel);

        for (EligibilityRule rule : EligibilityRule.values()) {
            if (!CompositeRules.isComposite(rule)) {
                assertTrue(map.containsKey(rule));
            }
        }
    }
}