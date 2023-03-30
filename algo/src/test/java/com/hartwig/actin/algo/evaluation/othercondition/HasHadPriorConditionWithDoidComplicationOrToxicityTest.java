package com.hartwig.actin.algo.evaluation.othercondition;

import static org.assertj.core.api.Assertions.assertThat;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationAssert;
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
        Evaluation evaluation = victim.evaluate(TestDataFactory.createMinimalTestPatientRecord());
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation);
        assertThat(evaluation.failSpecificMessages()).containsOnly("test");
        assertThat(evaluation.failGeneralMessages()).containsOnly("test");
    }

}