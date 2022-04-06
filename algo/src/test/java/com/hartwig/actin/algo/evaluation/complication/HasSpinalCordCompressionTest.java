package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;

import org.junit.Test;

public class HasSpinalCordCompressionTest {

    @Test
    public void canEvaluate() {
        HasSpinalCordCompression function = new HasSpinalCordCompression();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ImmutableComplication.builder().name("other complication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication matching = ImmutableComplication.builder().name("this is spinal cord weak compression").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)));
    }
}