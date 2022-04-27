package com.hartwig.actin.treatment.trial;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.datamodel.TestTreatmentFactory;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.junit.Test;

public class EligibilityRuleUsageEvaluatorTest {

    @Test
    public void canEvaluateEligibilityRuleUsage() {
        List<Trial> trials =
                Lists.newArrayList(TestTreatmentFactory.createMinimalTestTrial(), TestTreatmentFactory.createProperTestTrial());

        EligibilityRuleUsageEvaluator.evaluate(trials);
    }
}