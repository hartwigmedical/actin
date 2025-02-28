package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val INELIGIBLE_COHORT = "INELIGIBLE"
private const val CLOSED_COHORT = "CLOSED"
private const val ELIGIBLE_COHORT = "ELIGIBLE"
private const val ELIGIBLE_COHORT_2 = "ELIGIBLE2"
private const val ELIGIBLE_EVENT = "event"
private const val LOCATION = "location"

class InterpretedCohortsSummarizerTest {

    @Test
    fun `Should return all eligible and open cohorts for driver`() {
        val matchingTrials = createInterpreter().trialsForDriver(driverForEvent(ELIGIBLE_EVENT))
        assertThat(matchingTrials).containsExactlyInAnyOrder(
            TrialAcronymAndLocations(ELIGIBLE_COHORT, listOf(LOCATION)),
            TrialAcronymAndLocations(ELIGIBLE_COHORT_2, emptyList())
        )
    }

    @Test
    fun `Should not return matches for ineligible cohorts`() {
        assertThat(createInterpreter().trialsForDriver(driverForEvent(INELIGIBLE_COHORT))).isEmpty()
    }

    @Test
    fun `Should not return matches for closed cohorts`() {
        assertThat(createInterpreter().trialsForDriver(driverForEvent(CLOSED_COHORT))).isEmpty()
    }

    @Test
    fun `Should indicate driver is actionable if event matches open trial`() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        assertThat(createInterpreter().driverIsActionable(driverForEvent(ELIGIBLE_EVENT))).isTrue
    }

    @Test
    fun `Should indicate driver is actionable if external trial exists`() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.createMinimal().copy(
            event = CLOSED_COHORT,
            evidence = TestClinicalEvidenceFactory.withEligibleTrial(TestExternalTrialFactory.createTestTrial())
        )
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    @Test
    fun `Should indicate driver is actionable if approved treatment exists`() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.createMinimal().copy(
            event = CLOSED_COHORT,
            evidence = TestClinicalEvidenceFactory.withApprovedTreatment("treatment")
        )
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    private fun driverForEvent(event: String): Driver {
        return TestVariantFactory.createMinimal().copy(event = event)
    }

    private fun interpretedCohort(
        name: String,
        isEligible: Boolean,
        isOpen: Boolean,
        event: String = name,
        locations: List<String> = emptyList()
    ): InterpretedCohort {
        return InterpretedCohortTestFactory.interpretedCohort(
            acronym = name,
            isPotentiallyEligible = isEligible,
            isOpen = isOpen,
            molecularEvents = setOf(event),
            locations = locations
        )
    }

    private fun createInterpreter(): InterpretedCohortsSummarizer {
        return InterpretedCohortsSummarizer.fromCohorts(
            listOf(
                interpretedCohort(INELIGIBLE_COHORT, isEligible = false, isOpen = true),
                interpretedCohort(CLOSED_COHORT, isEligible = true, isOpen = false),
                interpretedCohort(ELIGIBLE_COHORT, isEligible = true, isOpen = true, ELIGIBLE_EVENT, listOf(LOCATION)),
                interpretedCohort(ELIGIBLE_COHORT_2, isEligible = true, isOpen = true, ELIGIBLE_EVENT)
            )
        )
    }
}