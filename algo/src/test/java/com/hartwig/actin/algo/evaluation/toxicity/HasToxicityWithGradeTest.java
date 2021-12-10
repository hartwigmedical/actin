package com.hartwig.actin.algo.evaluation.toxicity;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasToxicityWithGradeTest {

    @Test
    public void canEvaluateGradeOnly() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(withToxicities(toxicities)));

        toxicities.add(builder().grade(1).build());
        assertEquals(Evaluation.FAIL, function.evaluate(withToxicities(toxicities)));

        toxicities.add(builder().grade(2).build());
        assertEquals(Evaluation.PASS, function.evaluate(withToxicities(toxicities)));
    }

    @Test
    public void canEvaluateQuestionnaireToxicity() {
        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(builder().source(ToxicitySource.QUESTIONNAIRE).build());

        HasToxicityWithGrade match = new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE, null, Sets.newHashSet());
        assertEquals(Evaluation.PASS, match.evaluate(withToxicities(toxicities)));

        HasToxicityWithGrade noMatch =
                new HasToxicityWithGrade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 1, null, Sets.newHashSet());
        assertEquals(Evaluation.UNDETERMINED, noMatch.evaluate(withToxicities(toxicities)));

        toxicities.add(builder().grade(HasToxicityWithGrade.DEFAULT_QUESTIONNAIRE_GRADE + 2).build());
        assertEquals(Evaluation.PASS, noMatch.evaluate(withToxicities(toxicities)));
    }

    @Test
    public void canIgnoreToxicities() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, null, Sets.newHashSet("ignore"));

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(builder().grade(2).name("ignore").build());
        assertEquals(Evaluation.FAIL, function.evaluate(withToxicities(toxicities)));

        toxicities.add(builder().grade(2).name("do not ignore").build());
        assertEquals(Evaluation.PASS, function.evaluate(withToxicities(toxicities)));
    }

    @Test
    public void canFilterOnSpecificToxicity() {
        HasToxicityWithGrade function = new HasToxicityWithGrade(2, "specific", Sets.newHashSet());

        List<Toxicity> toxicities = Lists.newArrayList();
        toxicities.add(builder().grade(2).name("not specific").build());
        assertEquals(Evaluation.FAIL, function.evaluate(withToxicities(toxicities)));

        toxicities.add(builder().grade(2).name("specific").build());
        assertEquals(Evaluation.PASS, function.evaluate(withToxicities(toxicities)));

    }

    @NotNull
    private static PatientRecord withToxicities(@NotNull List<Toxicity> toxicities) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .toxicities(toxicities)
                        .build())
                .build();
    }

    @NotNull
    private static ImmutableToxicity.Builder builder() {
        return ImmutableToxicity.builder().name(Strings.EMPTY).evaluatedDate(LocalDate.of(2020, 1, 1)).source(ToxicitySource.EHR);
    }
}