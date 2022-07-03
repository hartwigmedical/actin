package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHistoryOfPneumonitisTest {

    @Test
    public void canEvaluateOnPriorOtherConditions() {
        HasHistoryOfPneumonitis function = createTestPneumonitisFunction();

        // Test empty doid
        List<PriorOtherCondition> conditions = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with wrong doid
        conditions.add(OtherConditionTestFactory.builder().addDoids("wrong doid").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));

        // Add a condition with correct DOID
        String pneumonitisDoid = HasHistoryOfPneumonitis.PNEUMONITIS_DOID;
        conditions.add(OtherConditionTestFactory.builder().addDoids(pneumonitisDoid).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)));
    }

    @Test
    public void canEvaluateOnToxicities() {
        HasHistoryOfPneumonitis function = createTestPneumonitisFunction();

        // Test no toxicities
        List<Toxicity> toxicities = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));

        // Add an irrelevant toxicity
        toxicities.add(toxicityBuilder().name("Not a relevant one").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withToxicities(toxicities)));

        String relevantToxicity = HasHistoryOfPneumonitis.TOXICITIES_CAUSING_PNEUMONITIS.iterator().next();
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
    private static HasHistoryOfPneumonitis createTestPneumonitisFunction() {
        return new HasHistoryOfPneumonitis(TestDoidModelFactory.createMinimalTestDoidModel());
    }
}