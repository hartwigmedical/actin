package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.junit.Test;

public class HasComplicationOfCategoryTest {

    @Test
    public void canEvaluate() {
        HasComplicationOfCategory function = new HasComplicationOfCategory("category X");

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComplicationTestFactory.withComplications(null)));
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(ComplicationTestFactory.withComplications(List.of(ComplicationTestFactory.yesInputComplication()))));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ComplicationTestFactory.builder().addCategories("this is category Y").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication match = ComplicationTestFactory.builder().addCategories("this is category X").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)));
    }
}