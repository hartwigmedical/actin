package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasContraindicationToMRITest {

    @Test
    public void canEvaluateOnPriorOtherCondition() {
        HasContraindicationToMRI function = createTestContraindicationMRIFunction();

        // Test no prior other conditions
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(Lists.newArrayList())));

        // Add a condition with wrong doid
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids("wrong doid")
                        .build())));

        // Add a condition with correct DOID
        String contraindicationDoid = HasContraindicationToMRI.KIDNEY_DISEASE_DOID;
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids(contraindicationDoid)
                        .build())));

        // Test with a condition with wrong name
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .name("not a contraindication")
                        .build())));

        // Test with a condition with correct name
        String contraindicationName = HasContraindicationToMRI.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI.iterator().next();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .name(contraindicationName)
                        .build())));
    }

    @Test
    public void canEvaluateOnIntolerance() {
        HasContraindicationToMRI function = createTestContraindicationMRIFunction();

        // Test without intolerances
        List<Intolerance> intolerances = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));

        // Test no relevant intolerance
        intolerances.add(intoleranceBuilder().name("no relevant intolerance").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));

        // Test relevant intolerance
        String relevantIntolerance = HasContraindicationToMRI.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI.iterator().next();
        intolerances.add(intoleranceBuilder().name(relevantIntolerance).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));
    }

    @NotNull
    private static ImmutableIntolerance.Builder intoleranceBuilder() {
        return ImmutableIntolerance.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .type(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY);
    }

    @NotNull
    private static HasContraindicationToMRI createTestContraindicationMRIFunction() {
        return new HasContraindicationToMRI(TestDoidModelFactory.createMinimalTestDoidModel());
    }

}