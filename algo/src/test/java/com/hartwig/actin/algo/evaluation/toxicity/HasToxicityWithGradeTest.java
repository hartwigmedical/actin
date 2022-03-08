package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;

import org.junit.Test;

public class HasToxicityWithGradeTest {

    @Test
    public void canEvaluateGradeOnly() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.builder().grade(1).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.builder().grade(2).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canEvaluateQuestionnaireToxicity() {
        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.builder().source(ToxicitySource.QUESTIONNAIRE).build());

        HasToxicityWithGrade match = new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE, null, Sets.newHashSet());
        assertEvaluation(EvaluationResult.PASS, match.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        HasToxicityWithGrade noMatch =
                new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 1, null, Sets.newHashSet());
        assertEvaluation(EvaluationResult.UNDETERMINED, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.builder().grade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 2).build());
        assertEvaluation(EvaluationResult.PASS, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canIgnoreToxicities() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet("ignore"));

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.builder().grade(2).name("ignore me please").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.builder().grade(2).name("keep me please").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canFilterOnSpecificToxicity() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, "specific", Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.builder().grade(2).name("something random").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.builder().grade(2).name("something specific").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }
}