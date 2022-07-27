package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasPotentialSignificantHeartDiseaseTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();

        HasPotentialSignificantHeartDisease function = new HasPotentialSignificantHeartDisease(doidModel);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true)));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(builder().build())));

        String firstDoid = HasPotentialSignificantHeartDisease.HEART_DISEASE_DOIDS.iterator().next();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(builder().addDoids(firstDoid).build())));

        String firstTerm = HasPotentialSignificantHeartDisease.HEART_DISEASE_TERMS.iterator().next();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(builder().name("this is a " + firstTerm).build())));
    }

    @NotNull
    private static ImmutablePriorOtherCondition.Builder builder() {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true);
    }
}