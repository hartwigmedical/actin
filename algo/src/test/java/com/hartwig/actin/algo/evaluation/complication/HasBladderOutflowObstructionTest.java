package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class HasBladderOutflowObstructionTest {

    @Test
    public void canEvaluateOnComplication() {
        HasBladderOutflowObstruction function = new HasBladderOutflowObstruction();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ImmutableComplication.builder().name("other complication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication matching = ImmutableComplication.builder().name("this is bladder gain of retention").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)));
    }

    @Test
    public void canEvaluateOnPriorOtherCondition() {
        HasBladderOutflowObstruction function = new HasBladderOutflowObstruction();

        PriorOtherCondition different = ImmutablePriorOtherCondition.builder()
                .name("different")
                .category(Strings.EMPTY)
                .isContraindicationForTherapy(true)
                .build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(different)));

        PriorOtherCondition noContraindication = ImmutablePriorOtherCondition.builder()
                .name("bladder obstruction")
                .addDoids(HasBladderOutflowObstruction.BLADDER_NECK_OBSTRUCTION_DOID)
                .category(Strings.EMPTY)
                .isContraindicationForTherapy(false)
                .build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(noContraindication)));

        PriorOtherCondition matching = ImmutablePriorOtherCondition.builder()
                .name("bladder obstruction")
                .addDoids(HasBladderOutflowObstruction.BLADDER_NECK_OBSTRUCTION_DOID)
                .category(Strings.EMPTY)
                .isContraindicationForTherapy(true)
                .build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(matching)));
    }
}