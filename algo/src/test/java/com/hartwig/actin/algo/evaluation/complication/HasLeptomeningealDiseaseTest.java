package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

import org.junit.Test;

public class HasLeptomeningealDiseaseTest {

    @Test
    public void canEvaluate() {
        HasLeptomeningealDisease function = new HasLeptomeningealDisease();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ImmutableComplication.builder().name("other complication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication matching = ImmutableComplication.builder().name("this is a carcinomatous serious meningitis for sure").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withCnsLesion("just a lesion")));
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ComplicationTestFactory.withCnsLesion("carcinomatous meningitis")));
    }
}