package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.junit.Test;

public class HasSpecificComplicationTest {

    @Test
    public void canEvaluate() {
        HasSpecificComplication function = new HasSpecificComplication("name to find");

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(ComplicationTestFactory.withComplications(List.of(ComplicationTestFactory.yesInputComplication()))));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication wrong = ComplicationTestFactory.builder().name("just a name").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)));

        Complication match = ComplicationTestFactory.builder().name("this includes name to find").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)));
    }
}