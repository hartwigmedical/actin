package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.ImmutableAllergy;

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
    public void canEvaluateOnAllergy() {
        HasContraindicationToMRI function = createTestContraindicationMRIFunction();

        // Test without allergies
        List<Allergy> allergies = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withAllergies(allergies)));

        // Test no relevant allergy
        allergies.add(allergyBuilder().name("no relevant allergy").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withAllergies(allergies)));

        // Test relevant allergy
        String relevantAllergy = HasContraindicationToMRI.ALLERGIES_BEING_CONTRAINDICATIONS_TO_MRI.iterator().next();
        allergies.add(allergyBuilder().name(relevantAllergy).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withAllergies(allergies)));
    }

    @NotNull
    private static ImmutableAllergy.Builder allergyBuilder() {
        return ImmutableAllergy.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY);
    }

    @NotNull
    private static HasContraindicationToMRI createTestContraindicationMRIFunction() {
        return new HasContraindicationToMRI(TestDoidModelFactory.createMinimalTestDoidModel());
    }

}