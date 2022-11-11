package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedTrialFactoryTest {

    @Test
    public void canCreateEvaluatedTrials() {
        TreatmentMatch treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch();
        List<EvaluatedTrial> trials = EvaluatedTrialFactory.create(treatmentMatch);

        assertEquals(5, trials.size());

        EvaluatedTrial trial1A = findByTrialAndCohort(trials, "TEST-1", "Cohort A");
        assertFalse(trial1A.molecularEvents().isEmpty());
        assertTrue(trial1A.isPotentiallyEligible());
        assertTrue(trial1A.isOpen());
        assertFalse(trial1A.hasSlotsAvailable());
        assertFalse(trial1A.warnings().isEmpty());
        assertTrue(trial1A.fails().isEmpty());

        EvaluatedTrial trial1B = findByTrialAndCohort(trials, "TEST-1", "Cohort B");
        assertTrue(trial1B.molecularEvents().isEmpty());
        assertTrue(trial1B.isPotentiallyEligible());
        assertTrue(trial1B.isOpen());
        assertTrue(trial1B.hasSlotsAvailable());
        assertFalse(trial1B.warnings().isEmpty());
        assertTrue(trial1B.fails().isEmpty());

        EvaluatedTrial trial1C = findByTrialAndCohort(trials, "TEST-1", "Cohort C");
        assertTrue(trial1C.molecularEvents().isEmpty());
        assertFalse(trial1C.isPotentiallyEligible());
        assertFalse(trial1C.isOpen());
        assertFalse(trial1C.hasSlotsAvailable());
        assertFalse(trial1C.warnings().isEmpty());
        assertFalse(trial1C.fails().isEmpty());

        EvaluatedTrial trial2A = findByTrialAndCohort(trials, "TEST-2", "Cohort A");
        assertFalse(trial2A.molecularEvents().isEmpty());
        assertTrue(trial2A.isPotentiallyEligible());
        assertTrue(trial2A.isOpen());
        assertFalse(trial2A.hasSlotsAvailable());
        assertTrue(trial2A.warnings().isEmpty());
        assertTrue(trial2A.fails().isEmpty());

        EvaluatedTrial trial2B = findByTrialAndCohort(trials, "TEST-2", "Cohort B");
        assertTrue(trial2B.molecularEvents().isEmpty());
        assertFalse(trial2B.isPotentiallyEligible());
        assertTrue(trial2B.isOpen());
        assertTrue(trial2B.hasSlotsAvailable());
        assertTrue(trial2B.warnings().isEmpty());
        assertFalse(trial2B.fails().isEmpty());
    }

    @Test
    public void canAnnotateWithMolecularEvents() {
        TreatmentMatch treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch();

        List<EvaluatedTrial> trials = EvaluatedTrialFactory.create(treatmentMatch);

        assertTrue(findByTrialAndCohort(trials, "TEST-1", "Cohort A").molecularEvents().contains("BRAF V600E"));
        assertTrue(findByTrialAndCohort(trials, "TEST-1", "Cohort B").molecularEvents().isEmpty());
        assertTrue(findByTrialAndCohort(trials, "TEST-1", "Cohort C").molecularEvents().isEmpty());
        assertTrue(findByTrialAndCohort(trials, "TEST-2", "Cohort A").molecularEvents().contains("BRAF V600E"));
        assertTrue(findByTrialAndCohort(trials, "TEST-2", "Cohort B").molecularEvents().isEmpty());
    }

    @NotNull
    private static EvaluatedTrial findByTrialAndCohort(@NotNull List<EvaluatedTrial> trials, @NotNull String trialToFind,
            @Nullable String cohortToFind) {
        for (EvaluatedTrial trial : trials) {
            if (trial.acronym().equals(trialToFind) && Objects.equals(trial.cohort(), cohortToFind)) {
                return trial;
            }
        }

        throw new IllegalStateException("Could not find trial for cohort: " + cohortToFind);
    }
}