package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.ParameterizedFunctionTestFactory;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.junit.Test;

public class FunctionCreatorFactoryTest {

    @Test
    public void everyFunctionCanBeCreated() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();
        ReferenceDateProvider referenceDateProvider = ReferenceDateProviderTestFactory.createCurrentDateProvider();
        Map<EligibilityRule, FunctionCreator> map = FunctionCreatorFactory.create(doidModel, referenceDateProvider);

        for (EligibilityRule rule : EligibilityRule.values()) {
            EligibilityFunction function = ParameterizedFunctionTestFactory.create(rule);
            if (!CompositeRules.isComposite(rule)) {
                FunctionCreator creator = map.get(rule);
                assertNotNull(rule + " has no creator configured", creator);
                assertNotNull(rule + " creator could not create function", creator.create(function));
            }
        }
    }
}