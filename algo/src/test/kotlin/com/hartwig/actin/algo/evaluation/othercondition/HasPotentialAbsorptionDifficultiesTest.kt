package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasPotentialAbsorptionDifficultiesTest {

    @Test
    public void canEvaluateOnPriorOtherConditions() {
        HasPotentialAbsorptionDifficulties function = createTestAbsorptionFunction();

        // Test empty doid
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with wrong doid
        conditions.add(OtherConditionTestFactory.builder().addDoids("wrong doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with correct DOID
        String absorptionDoid = DoidConstants.GASTROINTESTINAL_SYSTEM_DISEASE_DOID;
        conditions.add(OtherConditionTestFactory.builder().addDoids(absorptionDoid).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));
    }

    @Test
    public void canEvaluateOnComplications() {
        HasPotentialAbsorptionDifficulties function = createTestAbsorptionFunction();

        // Test no complications
        List<Complication> complications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)));

        // Add a random complication
        complications.add(ImmutableComplication.builder().name("not a problem").addCategories("random complication").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(complications)));

        // Add a real absorption one
        complications.add(ImmutableComplication.builder()
                .name("real complication")
                .addCategories(HasPotentialAbsorptionDifficulties.GASTROINTESTINAL_DISORDER_CATEGORY)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(complications)));
    }

    @Test
    public void canEvaluateOnToxicities() {
        HasPotentialAbsorptionDifficulties function = createTestAbsorptionFunction();

        // Test no toxicities
        List<Toxicity> toxicities = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));

        // Add an irrelevant toxicity
        toxicities.add(toxicityBuilder().name("Not a relevant one").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));

        String relevantToxicity = HasPotentialAbsorptionDifficulties.TOXICITIES_CAUSING_ABSORPTION_DIFFICULTY.iterator().next();
        // Add a toxicity with too low grade
        toxicities.add(toxicityBuilder().name(relevantToxicity).source(ToxicitySource.EHR).grade(1).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));

        // Add a valid toxicity
        toxicities.add(toxicityBuilder().name(relevantToxicity).source(ToxicitySource.EHR).grade(3).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));
    }

    @NotNull
    public static ImmutableToxicity.Builder toxicityBuilder() {
        return ImmutableToxicity.builder().name(Strings.EMPTY).evaluatedDate(LocalDate.of(2010, 1, 1)).source(ToxicitySource.QUESTIONNAIRE);
    }

    @NotNull
    private static HasPotentialAbsorptionDifficulties createTestAbsorptionFunction() {
        return new HasPotentialAbsorptionDifficulties(TestDoidModelFactory.createMinimalTestDoidModel());
    }
}