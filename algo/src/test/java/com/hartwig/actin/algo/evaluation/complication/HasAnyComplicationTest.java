package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

import org.junit.Test;

public class HasAnyComplicationTest {

    @Test
    public void canEvaluate() {
        HasAnyComplication function = new HasAnyComplication();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(ComplicationTestFactory.withComplication(ComplicationTestFactory.builder().build())));
    }
}