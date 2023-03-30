package com.hartwig.actin.algo.evaluation.othercondition;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasHadPriorConditionWithDoidComplicationOrToxicityTest {

    @Test
    public void shouldEvaluateFailWhenNoMatchingDoidComplicationOrToxicity() {
        EvaluationFunction victim =
                new HasHadPriorConditionWithDoidComplicationOrToxicity(TestDoidModelFactory.createMinimalTestDoidModel(),
                        "1234",
                        "complication",
                        "toxicity");
        Evaluation evaluate = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        assertThat
    }

}