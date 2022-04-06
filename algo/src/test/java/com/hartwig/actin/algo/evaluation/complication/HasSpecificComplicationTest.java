package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

import org.junit.Test;

public class HasSpecificComplicationTest {

    @Test
    public void canEvaluate() {
        HasSpecificComplication function = new HasSpecificComplication("name to find");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication wrong = ImmutableComplication.builder().name("just a name").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)));

        Complication match = ImmutableComplication.builder().name("this includes name to find").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)));
    }
}