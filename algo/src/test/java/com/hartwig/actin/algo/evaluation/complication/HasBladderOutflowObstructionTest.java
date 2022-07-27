package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasBladderOutflowObstructionTest {

    @Test
    public void canEvaluateOnComplication() {
        HasBladderOutflowObstruction function = new HasBladderOutflowObstruction();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication different = ComplicationTestFactory.builder().name("other complication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(different)));

        Complication matching = ComplicationTestFactory.builder().name("this is bladder gain of retention").build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(matching)));
    }

    @Test
    public void canEvaluateOnPriorOtherCondition() {
        HasBladderOutflowObstruction function = new HasBladderOutflowObstruction();

        PriorOtherCondition different = priorOtherConditionBuilder().name("different").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(different)));

        PriorOtherCondition noContraindication = priorOtherConditionBuilder().name("bladder obstruction")
                .addDoids(HasBladderOutflowObstruction.BLADDER_NECK_OBSTRUCTION_DOID)
                .isContraindicationForTherapy(false)
                .build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(noContraindication)));

        PriorOtherCondition matching = priorOtherConditionBuilder().name("bladder obstruction")
                .addDoids(HasBladderOutflowObstruction.BLADDER_NECK_OBSTRUCTION_DOID)
                .build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withPriorOtherCondition(matching)));
    }

    @NotNull
    private static ImmutablePriorOtherCondition.Builder priorOtherConditionBuilder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true);
    }
}