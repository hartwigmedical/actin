package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.CompositeRules;
import com.hartwig.actin.treatment.interpretation.TestParameterizedFunctionFactory;

import org.junit.Test;

public class FunctionCreatorFactoryTest {

    @Test
    public void everyFunctionCanBeCreated() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        Map<EligibilityRule, FunctionCreator> map = FunctionCreatorFactory.createFunctionCreatorMap(doidModel);

        for (EligibilityRule rule : EligibilityRule.values()) {
            EligibilityFunction function = TestParameterizedFunctionFactory.create(rule);
            if (!CompositeRules.isComposite(rule) && rule != EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT
                    && rule != EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_OF_TYPE_Y) {
                FunctionCreator creator = map.get(rule);
                assertNotNull(creator.create(function));
            }
        }
    }
}