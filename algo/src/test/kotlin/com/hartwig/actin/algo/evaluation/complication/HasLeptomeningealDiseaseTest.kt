package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;

import org.junit.Test;

public class HasLeptomeningealDiseaseTest {

    @Test
    public void canEvaluate() {
        HasLeptomeningealDisease function = new HasLeptomeningealDisease();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ComplicationTestFactory.builder().addCategories("other complication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication matching = ComplicationTestFactory.builder().addCategories("leptomeningeal disease type 1").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withCnsLesion("just a lesion")));
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(ComplicationTestFactory.withCnsLesion("carcinomatous furious meningitis")));
    }
}