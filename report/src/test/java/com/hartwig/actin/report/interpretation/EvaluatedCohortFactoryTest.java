package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class EvaluatedCohortFactoryTest {

    @Test
    public void canCreateEvaluatedTrialsFromMinimalMatch() {
        List<EvaluatedCohort> trials = EvaluatedCohortFactory.create(TestTreatmentMatchFactory.createMinimalTreatmentMatch());

        assertTrue(trials.isEmpty());
    }

    @Test
    public void canCreateEvaluatedTrialsFromProperMatch() {
        List<EvaluatedCohort> trials = EvaluatedCohortFactory.create(TestTreatmentMatchFactory.createProperTreatmentMatch());

        assertEquals(5, trials.size());

        EvaluatedCohort trial1A = findByAcronymAndCohort(trials, "TEST-1", "Cohort A");
        assertFalse(trial1A.molecularEvents().isEmpty());
        assertTrue(trial1A.molecularEvents().contains("BRAF V600E"));
        assertTrue(trial1A.isPotentiallyEligible());
        assertTrue(trial1A.isOpen());
        assertFalse(trial1A.hasSlotsAvailable());
        assertFalse(trial1A.warnings().isEmpty());
        assertTrue(trial1A.fails().isEmpty());

        EvaluatedCohort trial1B = findByAcronymAndCohort(trials, "TEST-1", "Cohort B");
        assertTrue(trial1B.molecularEvents().isEmpty());
        assertTrue(trial1B.isPotentiallyEligible());
        assertTrue(trial1B.isOpen());
        assertTrue(trial1B.hasSlotsAvailable());
        assertFalse(trial1B.warnings().isEmpty());
        assertTrue(trial1B.fails().isEmpty());

        EvaluatedCohort trial1C = findByAcronymAndCohort(trials, "TEST-1", "Cohort C");
        assertTrue(trial1C.molecularEvents().isEmpty());
        assertFalse(trial1C.isPotentiallyEligible());
        assertFalse(trial1C.isOpen());
        assertFalse(trial1C.hasSlotsAvailable());
        assertFalse(trial1C.warnings().isEmpty());
        assertFalse(trial1C.fails().isEmpty());

        EvaluatedCohort trial2A = findByAcronymAndCohort(trials, "TEST-2", "Cohort A");
        assertFalse(trial2A.molecularEvents().isEmpty());
        assertTrue(trial2A.molecularEvents().contains("BRAF V600E"));
        assertTrue(trial2A.isPotentiallyEligible());
        assertTrue(trial2A.isOpen());
        assertFalse(trial2A.hasSlotsAvailable());
        assertTrue(trial2A.warnings().isEmpty());
        assertTrue(trial2A.fails().isEmpty());

        EvaluatedCohort trial2B = findByAcronymAndCohort(trials, "TEST-2", "Cohort B");
        assertTrue(trial2B.molecularEvents().isEmpty());
        assertFalse(trial2B.isPotentiallyEligible());
        assertTrue(trial2B.isOpen());
        assertTrue(trial2B.hasSlotsAvailable());
        assertTrue(trial2B.warnings().isEmpty());
        assertFalse(trial2B.fails().isEmpty());
    }

    @Test
    public void canEvaluateTrialsWithoutCohort() {
        TrialMatch trialMatchWithoutCohort = ImmutableTrialMatch.builder()
                .identification(ImmutableTrialIdentification.builder()
                        .trialId("test")
                        .open(true)
                        .acronym("test-1")
                        .title("Example test trial 1")
                        .build())
                .isPotentiallyEligible(true)
                .build();

        TreatmentMatch treatmentMatch = ImmutableTreatmentMatch.builder()
                .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .addTrialMatches(trialMatchWithoutCohort)
                .build();

        List<EvaluatedCohort> trials = EvaluatedCohortFactory.create(treatmentMatch);
        assertEquals(1, trials.size());
    }

    @NotNull
    private static EvaluatedCohort findByAcronymAndCohort(@NotNull List<EvaluatedCohort> trials, @NotNull String acronymToFind,
            @Nullable String cohortToFind) {
        for (EvaluatedCohort trial : trials) {
            if (trial.acronym().equals(acronymToFind) && Objects.equals(trial.cohort(), cohortToFind)) {
                return trial;
            }
        }

        throw new IllegalStateException("Could not find trial acronym " + acronymToFind + " and cohort: " + cohortToFind);
    }
}