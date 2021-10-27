package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.Trial;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TrialFactoryTest {

    private static final String TRIAL_CONFIG_DIRECTORY = Resources.getResource("trial_config").getPath();

    @Test
    public void canCreateFromTrialConfigDirectory() throws IOException {
        assertNotNull(TrialFactory.fromTrialConfigDirectory(TRIAL_CONFIG_DIRECTORY));
    }

    @Test
    public void canCreateFromProperTestModel() {
        List<Trial> trials = new TrialFactory(TestTrialConfigFactory.createProperTestTrialConfigModel()).create();

        assertEquals(1, trials.size());

        Trial trial = trials.get(0);
        assertEquals("TEST", trial.trialId());
        assertEquals("Acronym-TEST", trial.acronym());
        assertEquals("Title for TEST", trial.title());

        assertEquals(1, trial.generalEligibilityFunctions().size());

        EligibilityFunction generalFunction = trial.generalEligibilityFunctions().get(0);
        assertEquals(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, generalFunction.rule());
        assertTrue(generalFunction.parameters().isEmpty());

        assertEquals(3, trial.cohorts().size());

        Cohort cohortA = findCohort(trial.cohorts(), "A");
        assertEquals("Cohort A", cohortA.description());
        assertEquals(1, cohortA.eligibilityFunctions().size());

        EligibilityFunction cohortFunction = cohortA.eligibilityFunctions().get(0);
        assertEquals(EligibilityRule.IS_AT_LEAST_18_YEARS_OLD, cohortFunction.rule());
        assertTrue(cohortFunction.parameters().isEmpty());

        Cohort cohortB = findCohort(trial.cohorts(), "B");
        assertEquals("Cohort B", cohortB.description());
        assertTrue(cohortB.eligibilityFunctions().isEmpty());

        Cohort cohortC = findCohort(trial.cohorts(), "C");
        assertEquals("Cohort C", cohortC.description());
        assertTrue(cohortC.eligibilityFunctions().isEmpty());
    }

    @NotNull
    private static Cohort findCohort(@NotNull List<Cohort> cohorts, @NotNull String cohortId) {
        for (Cohort cohort : cohorts) {
            if (cohort.cohortId().equals(cohortId)) {
                return cohort;
            }
        }

        throw new IllegalStateException("Could not find cohort with ID: " + cohortId);
    }

    @Test
    public void canGenerateSimpleEligibilityFunction() {
        EligibilityFunction function = TrialFactory.generateEligibilityFunction("HAS_INR_ULN_AT_MOST_X", Lists.newArrayList("1"));
        assertEquals(EligibilityRule.HAS_INR_ULN_AT_MOST_X, function.rule());
        assertEquals(1, function.parameters().size());
        assertTrue(function.parameters().contains("1"));

        EligibilityFunction notFunction = TrialFactory.generateEligibilityFunction("NOT(HAS_INR_ULN_AT_MOST_X)", Lists.newArrayList("1"));
        assertEquals(EligibilityRule.NOT, notFunction.rule());
        assertEquals(1, notFunction.parameters().size());

        EligibilityFunction subFunction = findFunction(notFunction.parameters(), EligibilityRule.HAS_INR_ULN_AT_MOST_X);
        assertEquals(function, subFunction);
    }

    @Test
    public void canGenerateComplexCompositeEligibilityFunction() {
        String criterion = "OR(IS_PREGNANT, AND(OR(HAS_INR_ULN_AT_MOST_X, HAS_PT_ULN_AT_MOST_X), HAS_APTT_ULN_AT_MOST_X))";

        List<String> parameters = Lists.newArrayList("1.5", "2", "3");

        EligibilityFunction orRoot = TrialFactory.generateEligibilityFunction(criterion, parameters);

        assertEquals(EligibilityRule.OR, orRoot.rule());
        assertEquals(2, orRoot.parameters().size());

        EligibilityFunction orRootInput1 = findFunction(orRoot.parameters(), EligibilityRule.IS_PREGNANT);
        assertEquals(0, orRootInput1.parameters().size());

        EligibilityFunction orRootInput2 = findFunction(orRoot.parameters(), EligibilityRule.AND);
        assertEquals(2, orRootInput2.parameters().size());

        EligibilityFunction andInput1 = findFunction(orRootInput2.parameters(), EligibilityRule.OR);
        assertEquals(2, andInput1.parameters().size());

        EligibilityFunction andInput2 = findFunction(orRootInput2.parameters(), EligibilityRule.HAS_APTT_ULN_AT_MOST_X);
        assertEquals(1, andInput2.parameters().size());
        assertTrue(andInput2.parameters().contains("3"));

        EligibilityFunction secondOrInput1 = findFunction(andInput1.parameters(), EligibilityRule.HAS_INR_ULN_AT_MOST_X);
        assertEquals(1, secondOrInput1.parameters().size());
        assertTrue(secondOrInput1.parameters().contains("1.5"));

        EligibilityFunction secondOrInput2 = findFunction(andInput1.parameters(), EligibilityRule.HAS_PT_ULN_AT_MOST_X);
        assertEquals(1, secondOrInput2.parameters().size());
        assertTrue(secondOrInput2.parameters().contains("2"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void crashOnIncorrectParamCount() {
        TrialFactory.generateEligibilityFunction("HAS_INR_ULN_AT_MOST_X", Lists.newArrayList());
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnInvalidCompositeFunction() {
        TrialFactory.generateEligibilityFunction("IS_PREGNANT(HAS_INR_ULN_AT_MOST_X)", Lists.newArrayList("1"));
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnWronglyFormattedCompositeFunction() {
        TrialFactory.generateEligibilityFunction("NOT(IS_PREGNANT", Lists.newArrayList());
    }

    @NotNull
    private static EligibilityFunction findFunction(@NotNull List<Object> functions, @NotNull EligibilityRule rule) {
        for (Object function : functions) {
            EligibilityFunction eligibilityFunction = (EligibilityFunction) function;
            if (eligibilityFunction.rule() == rule) {
                return eligibilityFunction;
            }
        }

        throw new IllegalStateException("Could not find eligibility function with rule " + rule);
    }
}