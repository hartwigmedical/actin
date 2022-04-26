package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Intolerance;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasContraindicationToCTTest {

    @Test
    public void canEvaluateOnPriorOtherCondition() {
        HasContraindicationToCT function = createTestContraindicationCTFunction();

        // Test no prior other conditions
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(Lists.newArrayList())));

        // Add a condition with wrong doid
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .addDoids("wrong doid")
                        .build())));

        // Add a condition with correct DOID
        String contraindicationDoid = HasContraindicationToCT.KIDNEY_DISEASE_DOID;
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
        String contraindicationName = HasContraindicationToCT.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.builder()
                        .name(contraindicationName)
                        .build())));
    }

    @Test
    public void canEvaluateOnIntolerance() {
        HasContraindicationToCT function = createTestContraindicationCTFunction();

        // Test without intolerances
        List<Intolerance> intolerances = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));

        // Test no relevant intolerance
        intolerances.add(intoleranceBuilder().name("no relevant allergy").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));

        // Test relevant intolerance
        String relevantAllergy = HasContraindicationToCT.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT.iterator().next();
        intolerances.add(intoleranceBuilder().name(relevantAllergy).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)));
    }

    @Test
    public void canEvaluateOnMedication() {
        HasContraindicationToCT function = createTestContraindicationCTFunction();

        // Test without medications
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(medications)));

        // Test no relevant medication
        medications.add(ImmutableMedication.builder().name("no relevant medication").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(medications)));

        // Test relevant medication
        String relevantMedication = HasContraindicationToCT.MEDICATIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next();
        medications.add(ImmutableMedication.builder().name(relevantMedication).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withMedications(medications)));
    }

    @Test
    public void canEvaluateOnComplication() {
        HasContraindicationToCT function = createTestContraindicationCTFunction();

        // Test without complications
        List<Complication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(medications)));

        // Test no relevant complication
        medications.add(ImmutableComplication.builder().name("no relevant complication").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(medications)));

        // Test relevant medication
        String relevantComplication = HasContraindicationToCT.COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next();
        medications.add(ImmutableComplication.builder().name(relevantComplication).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(medications)));
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
    private static HasContraindicationToCT createTestContraindicationCTFunction() {
        return new HasContraindicationToCT(TestDoidModelFactory.createMinimalTestDoidModel());
    }
}