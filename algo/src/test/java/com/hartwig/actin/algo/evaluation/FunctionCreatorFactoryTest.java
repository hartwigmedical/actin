package com.hartwig.actin.algo.evaluation;

import static org.junit.Assert.assertNotNull;

import java.util.Map;

import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.ParameterizedFunctionTestFactory;
import com.hartwig.actin.treatment.input.composite.CompositeRules;

import org.junit.Test;

public class FunctionCreatorFactoryTest {

    @Test
    public void everyFunctionCanBeCreated() {
        String doidTerm = "term 1";

        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("doid 1", doidTerm);
        ReferenceDateProvider referenceDateProvider = ReferenceDateProviderTestFactory.createCurrentDateProvider();
        FunctionInputResolver functionInputResolver = TestFunctionInputResolveFactory.createResolverWithDoidModel(doidModel);
        Map<EligibilityRule, FunctionCreator> map = FunctionCreatorFactory.create(referenceDateProvider, doidModel, functionInputResolver);

        ParameterizedFunctionTestFactory factory = new ParameterizedFunctionTestFactory(doidTerm);
        for (EligibilityRule rule : EligibilityRule.values()) {
            EligibilityFunction function = factory.create(rule);
            if (!CompositeRules.isComposite(rule)) {
                FunctionCreator creator = map.get(rule);
                assertNotNull(rule + " has no creator configured", creator);
                assertNotNull(rule + " creator could not create function", creator.create(function));
            }
        }
    }
}