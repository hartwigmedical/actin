package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.junit.Test;

public class EvaluatedCohortsInterpreterTest {

    private static final String INELIGIBLE_COHORT = "INELIGIBLE";
    private static final String CLOSED_COHORT = "CLOSED";
    private static final String ELIGIBLE_COHORT = "ELIGIBLE";
    private static final String ELIGIBLE_COHORT_2 = "ELIGIBLE2";

    @Test
    public void shouldReturnAllEligibleAndOpenCohortsForDriver() {
        List<String> matchingTrials = createInterpreter().trialsForDriver(driverForEvent(ELIGIBLE_COHORT));

        assertEquals(2, matchingTrials.size());
        assertTrue(matchingTrials.contains(ELIGIBLE_COHORT));
        assertTrue(matchingTrials.contains(ELIGIBLE_COHORT_2));
    }

    @Test
    public void shouldNotReturnMatchesForIneligibleCohorts() {
        assertTrue(createInterpreter().trialsForDriver(driverForEvent(INELIGIBLE_COHORT)).isEmpty());
    }

    @Test
    public void shouldNotReturnMatchesForClosedCohorts() {
        assertTrue(createInterpreter().trialsForDriver(driverForEvent(CLOSED_COHORT)).isEmpty());
    }

    @Test
    public void shouldIndicateDriverIsActionableIfEventMatchesEligibleTrial() {
        assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)));
        assertTrue(createInterpreter().driverIsActionable(driverForEvent(ELIGIBLE_COHORT)));
    }

    @Test
    public void shouldIndicateDriverIsActionableIfExternalTrialsEligible() {
        assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)));

        Driver driver = TestVariantFactory.builder()
                .event(INELIGIBLE_COHORT)
                .evidence(TestActionableEvidenceFactory.withExternalEligibleTrial("external"))
                .build();
        assertTrue(createInterpreter().driverIsActionable(driver));
    }

    @Test
    public void shouldIndicateDriverIsActionableIfApprovedTreatmentsExist() {
        assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)));

        Driver driver = TestVariantFactory.builder()
                .event(INELIGIBLE_COHORT)
                .evidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
                .build();
        assertTrue(createInterpreter().driverIsActionable(driver));
    }

    private static Driver driverForEvent(String event) {
        return TestVariantFactory.builder().event(event).build();
    }

    private static EvaluatedCohort evaluatedCohort(String name, boolean isEligible, boolean isOpen) {
        return evaluatedCohort(name, isEligible, isOpen, name);
    }

    private static EvaluatedCohort evaluatedCohort(String name, boolean isEligible, boolean isOpen, String event) {
        return EvaluatedCohortTestFactory.builder()
                .acronym(name)
                .isPotentiallyEligible(isEligible)
                .isOpen(isOpen)
                .addMolecularEvents(event)
                .build();
    }

    private static EvaluatedCohortsInterpreter createInterpreter() {
        return new EvaluatedCohortsInterpreter(List.of(evaluatedCohort(INELIGIBLE_COHORT, false, true),
                evaluatedCohort(CLOSED_COHORT, true, false),
                evaluatedCohort(ELIGIBLE_COHORT, true, true),
                evaluatedCohort(ELIGIBLE_COHORT_2, true, true, ELIGIBLE_COHORT)));
    }
}