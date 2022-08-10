package com.hartwig.actin.algo.evaluation.toxicity;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
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

        toxicities.add(ToxicityTestFactory.toxicity().source(ToxicitySource.QUESTIONNAIRE).grade(1).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity().source(ToxicitySource.QUESTIONNAIRE).grade(2).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canEvaluateQuestionnaireToxicity() {
        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.toxicity().source(ToxicitySource.QUESTIONNAIRE).build());

        HasToxicityWithGrade match = new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE, null, Sets.newHashSet());
        assertEvaluation(EvaluationResult.PASS, match.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        HasToxicityWithGrade noMatch =
                new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 1, null, Sets.newHashSet());
        assertEvaluation(EvaluationResult.UNDETERMINED, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity().grade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 2).build());
        assertEvaluation(EvaluationResult.PASS, noMatch.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canIgnoreToxicities() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet("ignore"));

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.toxicity().grade(2).name("ignore me please").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity().grade(2).name("keep me please").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void canFilterOnSpecificToxicity() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, "specific", Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.toxicity().grade(2).name("something random").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity().grade(2).name("something specific").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void picksOnlyMostRecentEHRToxicities() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, "specific", Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(ToxicityTestFactory.toxicity()
                .source(ToxicitySource.EHR)
                .grade(2)
                .name("specific match")
                .evaluatedDate(LocalDate.of(2020, 1, 1))
                .build());

        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity()
                .source(ToxicitySource.EHR)
                .grade(1)
                .name("specific match")
                .evaluatedDate(LocalDate.of(2021, 1, 1))
                .build());

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));

        toxicities.add(ToxicityTestFactory.toxicity()
                .source(ToxicitySource.EHR)
                .grade(3)
                .name("specific match")
                .evaluatedDate(LocalDate.of(2022, 1, 1))
                .build());

        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withToxicities(toxicities)));
    }

    @Test
    public void ignoresEHRToxicitiesThatAreAlsoComplications() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet());

        Toxicity questionnaireToxicity = ToxicityTestFactory.toxicity().source(ToxicitySource.QUESTIONNAIRE).grade(2).build();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(questionnaireToxicity)));

        Toxicity ehrToxicity = ToxicityTestFactory.toxicity().source(ToxicitySource.EHR).grade(2).build();
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(ToxicityTestFactory.withToxicityThatIsAlsoComplication(ehrToxicity)));
    }
}