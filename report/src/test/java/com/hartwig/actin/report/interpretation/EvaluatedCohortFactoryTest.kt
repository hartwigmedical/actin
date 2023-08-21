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
    public void canCreateEvaluatedCohortsFromMinimalMatch() {
        List<EvaluatedCohort> cohorts = EvaluatedCohortFactory.create(TestTreatmentMatchFactory.createMinimalTreatmentMatch());

        assertTrue(cohorts.isEmpty());
    }

    @Test
    public void canCreateEvaluatedCohortsFromProperMatch() {
        List<EvaluatedCohort> cohorts = EvaluatedCohortFactory.create(TestTreatmentMatchFactory.createProperTreatmentMatch());

        assertEquals(5, cohorts.size());

        EvaluatedCohort trial1cohortA = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort A");
        assertFalse(trial1cohortA.molecularEvents().isEmpty());
        assertTrue(trial1cohortA.molecularEvents().contains("BRAF V600E"));
        assertTrue(trial1cohortA.isPotentiallyEligible());
        assertTrue(trial1cohortA.isOpen());
        assertFalse(trial1cohortA.hasSlotsAvailable());
        assertFalse(trial1cohortA.warnings().isEmpty());
        assertTrue(trial1cohortA.fails().isEmpty());

        EvaluatedCohort trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B");
        assertTrue(trial1cohortB.molecularEvents().isEmpty());
        assertTrue(trial1cohortB.isPotentiallyEligible());
        assertTrue(trial1cohortB.isOpen());
        assertTrue(trial1cohortB.hasSlotsAvailable());
        assertFalse(trial1cohortB.warnings().isEmpty());
        assertTrue(trial1cohortB.fails().isEmpty());

        EvaluatedCohort trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C");
        assertTrue(trial1cohortC.molecularEvents().isEmpty());
        assertFalse(trial1cohortC.isPotentiallyEligible());
        assertFalse(trial1cohortC.isOpen());
        assertFalse(trial1cohortC.hasSlotsAvailable());
        assertFalse(trial1cohortC.warnings().isEmpty());
        assertFalse(trial1cohortC.fails().isEmpty());

        EvaluatedCohort trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A");
        assertFalse(trial2cohortA.molecularEvents().isEmpty());
        assertTrue(trial2cohortA.molecularEvents().contains("BRAF V600E"));
        assertTrue(trial2cohortA.isPotentiallyEligible());
        assertTrue(trial2cohortA.isOpen());
        assertFalse(trial2cohortA.hasSlotsAvailable());
        assertTrue(trial2cohortA.warnings().isEmpty());
        assertTrue(trial2cohortA.fails().isEmpty());

        EvaluatedCohort trial2cohortB = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort B");
        assertTrue(trial2cohortB.molecularEvents().isEmpty());
        assertFalse(trial2cohortB.isPotentiallyEligible());
        assertTrue(trial2cohortB.isOpen());
        assertTrue(trial2cohortB.hasSlotsAvailable());
        assertTrue(trial2cohortB.warnings().isEmpty());
        assertFalse(trial2cohortB.fails().isEmpty());
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

        List<EvaluatedCohort> cohorts = EvaluatedCohortFactory.create(treatmentMatch);
        assertEquals(1, cohorts.size());
    }

    @NotNull
    private static EvaluatedCohort findByAcronymAndCohort(@NotNull List<EvaluatedCohort> evaluatedCohorts, @NotNull String acronymToFind,
            @Nullable String cohortToFind) {
        for (EvaluatedCohort evaluatedCohort : evaluatedCohorts) {
            if (evaluatedCohort.acronym().equals(acronymToFind) && Objects.equals(evaluatedCohort.cohort(), cohortToFind)) {
                return evaluatedCohort;
            }
        }

        throw new IllegalStateException("Could not find trial acronym " + acronymToFind + " and cohort: " + cohortToFind);
    }
}