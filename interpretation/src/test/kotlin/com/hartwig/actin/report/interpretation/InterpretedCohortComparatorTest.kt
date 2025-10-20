package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InterpretedCohortComparatorTest {

    private val matchingLocation = TrialSource.EXAMPLE.description
    private val nonMatchingLocation = TrialSource.LKO.description
    private val cohort = create("trial 3", "cohort 1", emptySet(), true, "Event B")

    @Test
    fun `Should sort cohorts`() {
        val cohorts = listOf(
            create("trial 3", "first", setOf(matchingLocation, nonMatchingLocation), true, "Event B"),
            create("trial 3", "second", setOf(matchingLocation), false, "Event C"),
            create("trial 5", "third", setOf(nonMatchingLocation), false, "Event D", "Event A"),
            create("trial 7", "fourth", setOf(matchingLocation), true),
            create("trial 1", "fifth", setOf(nonMatchingLocation), true),
            create("trial 1", "A-sixth", setOf(nonMatchingLocation), false),
            create("trial 1", "B-seventh", setOf(nonMatchingLocation), false),
            create("trial 2", "eighth", setOf(nonMatchingLocation), false)
        )
        assertExpectedOrder(cohorts, TrialSource.EXAMPLE)
    }

    @Test
    fun `Should place cohorts from requesting source before those from other sources or source null`() {
        val cohort = cohort.copy(source = TrialSource.EXAMPLE)
        assertExpectedOrder(listOf(cohort, cohort.copy(source = TrialSource.LKO)), TrialSource.EXAMPLE)
        assertExpectedOrder(listOf(cohort, cohort.copy(source = null)), TrialSource.EXAMPLE)
    }

    @Test
    fun `Should check if trial matches to requestingSource using location if source is null`() {
        val cohort = cohort.copy(source = null, locations = setOf(TrialSource.NKI.description, TrialSource.EMC.description))
        assertExpectedOrder(listOf(cohort, cohort.copy(locations = setOf("some location"))), TrialSource.NKI)
    }

    @Test
    fun `Should place cohorts with open slots before those without`() {
        assertExpectedOrder(listOf(cohort, cohort.copy(hasSlotsAvailable = false)))
    }

    @Test
    fun `Should place cohorts with molecular events before cohorts without molecular events`() {
        assertExpectedOrder(
            listOf(
                cohort,
                cohort.copy(molecularInclusionEvents = emptySet())
            )
        )
    }

    @Test
    fun `Should prioritize later phases and put null phases last when comparing cohorts with phases`() {
        assertExpectedOrder(
            listOf(
                cohort.copy(phase = TrialPhase.PHASE_4),
                cohort.copy(phase = TrialPhase.PHASE_3),
                cohort.copy(phase = TrialPhase.PHASE_2_3),
                cohort.copy(phase = TrialPhase.PHASE_2),
                cohort.copy(phase = TrialPhase.PHASE_1_2),
                cohort.copy(phase = TrialPhase.PHASE_1),
                cohort
            )
        )
    }

    @Test
    fun `Should place cohorts with warnings after those without`() {
        assertExpectedOrder(listOf(cohort, cohort.copy(warnings = setOf("Warning"))))
    }

    private fun assertExpectedOrder(expectedCohorts: List<InterpretedCohort>, requestingSource: TrialSource? = null) {
        assertThat(expectedCohorts.reversed().sortedWith(InterpretedCohortComparator(requestingSource))).isEqualTo(expectedCohorts)
    }

    private fun create(
        trialId: String,
        cohort: String?,
        locations: Set<String>,
        hasSlotsAvailable: Boolean,
        vararg molecularEvents: String
    ): InterpretedCohort {
        return InterpretedCohortTestFactory.interpretedCohort(
            trialId = trialId,
            acronym = "",
            molecularInclusionEvents = setOf(*molecularEvents),
            cohort = cohort,
            locations = locations,
            isPotentiallyEligible = false,
            isOpen = false,
            hasSlotsAvailable = hasSlotsAvailable
        )
    }
}