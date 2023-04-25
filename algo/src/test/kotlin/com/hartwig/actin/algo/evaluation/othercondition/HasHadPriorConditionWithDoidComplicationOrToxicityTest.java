package com.hartwig.actin.algo.evaluation.othercondition;

import static com.hartwig.actin.ImmutablePatientRecord.copyOf;
import static com.hartwig.actin.TestDataFactory.createMinimalTestPatientRecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationAssert;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.ImmutableToxicity;
import com.hartwig.actin.clinical.datamodel.ToxicitySource;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

public class HasHadPriorConditionWithDoidComplicationOrToxicityTest {

    private static final String OTHER_CONDITION_NAME = "other condition";
    private static final String DOID = "1234";
    private static final PatientRecord PATIENT_RECORD = createMinimalTestPatientRecord();
    private static final String TOXICITY_CATEGORY = "toxicity_category";
    private static final String COMPLICATION_CATEGORY = "complication_category";
    private static final String COMPLICATION_NAME = "complication";
    private static final String TOXICITY_NAME = "toxicity";
    private static final String DOID_TERM = "some disease";

    private EvaluationFunction victim;

    @Before
    public void setUp() {
        victim = new HasHadPriorConditionWithDoidComplicationOrToxicity(TestDoidModelFactory.createWithOneDoidAndTerm(DOID, DOID_TERM),
                DOID,
                COMPLICATION_CATEGORY,
                TOXICITY_CATEGORY);
    }

    @Test
    public void shouldEvaluateFailWhenDoidNotPresentInModel() {
        victim = new HasHadPriorConditionWithDoidComplicationOrToxicity(TestDoidModelFactory.createMinimalTestDoidModel(),
                DOID,
                COMPLICATION_NAME,
                TOXICITY_NAME);
        Evaluation evaluation = victim.evaluate(PATIENT_RECORD);
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation);
        assertThat(evaluation.failSpecificMessages()).containsOnly("Patient has no other condition belonging to category unknown doid");
        assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition");
    }

    @Test
    public void shouldEvaluateFailWhenNoMatchingDoidComplicationOrToxicity() {
        Evaluation evaluation = victim.evaluate(PATIENT_RECORD);
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation);
        assertThat(evaluation.failSpecificMessages()).containsOnly("Patient has no other condition belonging to category some disease");
        assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition");
    }

    @Test
    public void shouldEvaluatePassWhenDoidTermMatchesCategory() {
        assertPassEvaluationWithMessages(victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                        .withPriorOtherConditions(priorOtherCondition()))),
                "Patient has condition(s) other condition, which is indicative of some disease");
    }

    @Test
    public void shouldEvaluatePassWhenComplicationMatchesCategory() {
        assertPassEvaluationWithMessages(victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                .withComplications(complication()))), "Patient has complication(s) complication, which is indicative of some disease");
    }

    @Test
    public void shouldEvaluatePassWhenToxicityFromQuestionnaireMatchesCategory() {
        assertPassEvaluationWithMessages(victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                        .withToxicities(toxicity(ToxicitySource.QUESTIONNAIRE, 1)))),
                "Patient has toxicity(ies) toxicity, which is indicative of some disease");
    }

    @Test
    public void shouldEvaluatePassWhenToxicityWithAtLeastGradeTwoMatchesCategory() {
        assertPassEvaluationWithMessages(victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                        .withToxicities(toxicity(ToxicitySource.EHR, 2)))),
                "Patient has toxicity(ies) toxicity, which is indicative of some disease");
    }

    @Test
    public void shouldEvaluateFailWhenToxicityLessThanGradeTwoAndSourceNotQuestionnaire() {
        Evaluation evaluation =
                victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                        .withToxicities(toxicity(ToxicitySource.EHR, 1))));
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, evaluation);
        assertThat(evaluation.failSpecificMessages()).containsOnly("Patient has no other condition belonging to category some disease");
        assertThat(evaluation.failGeneralMessages()).containsOnly("No relevant non-oncological condition");
    }

    @Test
    public void shouldIncludeMultipleMessages() {
        assertPassEvaluationWithMessages(victim.evaluate(copyOf(PATIENT_RECORD).withClinical(ImmutableClinicalRecord.copyOf(PATIENT_RECORD.clinical())
                        .withToxicities(toxicity(ToxicitySource.QUESTIONNAIRE, 2))
                        .withComplications(complication())
                        .withPriorOtherConditions(priorOtherCondition()))),
                "Patient has toxicity(ies) toxicity, which is indicative of some disease",
                "Patient has complication(s) complication, which is indicative of some disease",
                "Patient has condition(s) other condition, which is indicative of some disease");
    }

    @NotNull
    private static ImmutableComplication complication() {
        return ImmutableComplication.builder().addCategories(COMPLICATION_CATEGORY).name(COMPLICATION_NAME).build();
    }

    @NotNull
    private static ImmutableToxicity toxicity(ToxicitySource toxicitySource, Integer grade) {
        return ImmutableToxicity.builder()
                .addCategories(TOXICITY_CATEGORY)
                .name(TOXICITY_NAME)
                .evaluatedDate(LocalDate.now())
                .source(toxicitySource)
                .grade(grade)
                .build();
    }

    @NotNull
    private static ImmutablePriorOtherCondition priorOtherCondition() {
        return ImmutablePriorOtherCondition.builder()
                .addDoids(DOID)
                .name(OTHER_CONDITION_NAME)
                .category(DOID_TERM)
                .isContraindicationForTherapy(true)
                .build();
    }

    private static void assertPassEvaluationWithMessages(Evaluation evaluation, String... passSpecificMessages) {
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation);
        assertThat(evaluation.passSpecificMessages()).containsOnly(passSpecificMessages);
        assertThat(evaluation.passGeneralMessages()).containsOnly("Relevant non-oncological condition some disease");
    }
}