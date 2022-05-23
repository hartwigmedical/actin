package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidenceTestFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedTrialFactoryTest {

    @Test
    public void canCreateEvaluatedTrials() {
        TreatmentMatch treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch();
        List<EvaluatedTrial> trials = EvaluatedTrialFactory.create(treatmentMatch, Sets.newHashSet());

        assertEquals(4, trials.size());

        EvaluatedTrial cohortA = findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort A");
        assertTrue(cohortA.molecularEvents().isEmpty());
        assertFalse(cohortA.isPotentiallyEligible());
        assertTrue(cohortA.isOpen());
        assertTrue(cohortA.hasSlotsAvailable());
        assertTrue(cohortA.warnings().isEmpty());
        assertFalse(cohortA.fails().isEmpty());

        EvaluatedTrial cohortB = findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort B");
        assertTrue(cohortB.molecularEvents().isEmpty());
        assertTrue(cohortB.isPotentiallyEligible());
        assertTrue(cohortB.isOpen());
        assertFalse(cohortB.hasSlotsAvailable());
        assertTrue(cohortB.warnings().isEmpty());
        assertTrue(cohortB.fails().isEmpty());

        EvaluatedTrial cohortC = findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort C");
        assertTrue(cohortC.molecularEvents().isEmpty());
        assertFalse(cohortC.isPotentiallyEligible());
        assertFalse(cohortC.isOpen());
        assertTrue(cohortC.hasSlotsAvailable());
        assertTrue(cohortC.warnings().isEmpty());
        assertTrue(cohortC.fails().isEmpty());

        EvaluatedTrial trial2 = findByTrialAndCohort(trials, "TEST-TRIAL-2", null);
        assertTrue(trial2.molecularEvents().isEmpty());
        assertFalse(trial2.isPotentiallyEligible());
        assertTrue(trial2.isOpen());
        assertTrue(trial2.hasSlotsAvailable());
        assertFalse(trial2.warnings().isEmpty());
        assertFalse(trial2.fails().isEmpty());
    }

    @Test
    public void canAnnotateWithMolecularEvents() {
        TreatmentMatch treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch();
        ActinTrialEvidence evidence1 = ActinTrialEvidenceTestFactory.builder()
                .event("Event A")
                .isInclusionCriterion(true)
                .trialAcronym("TEST-TRIAL-1")
                .cohortId("A")
                .build();
        ActinTrialEvidence evidence2 = ActinTrialEvidenceTestFactory.builder()
                .event("Event B")
                .isInclusionCriterion(true)
                .trialAcronym("TEST-TRIAL-2")
                .cohortId(null)
                .build();

        List<EvaluatedTrial> trials = EvaluatedTrialFactory.create(treatmentMatch, Sets.newHashSet(evidence1, evidence2));

        assertTrue(findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort A").molecularEvents().contains("Event A"));
        assertTrue(findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort B").molecularEvents().isEmpty());
        assertTrue(findByTrialAndCohort(trials, "TEST-TRIAL-1", "Cohort C").molecularEvents().isEmpty());
        assertTrue(findByTrialAndCohort(trials, "TEST-TRIAL-2", null).molecularEvents().contains("Event B"));
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