package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InterpretedCohortComparatorTest {

    private val cohort = create("trial 3", "cohort 1", true, "Event B")

    @Test
    fun `Should sort cohorts`() {
        val cohorts = listOf(
            create("trial 7", "cohort 1", true),
            create("trial 3", "cohort 2 + cohort 3", false, "Event C"),
            create("trial 3", "cohort 1", false, "Event B"),
            create("trial 5", "cohort 1", false, "Event D", "Event A"),
            create("trial 5", "cohort 1", false, "Event C"),
            create("trial 1", null, false),
            create("trial 1", "cohort 1", false),
            create("trial 1", "cohort 2", false),
            create("trial 2", "cohort 1", false)
        )

        val cohortList = listOf(
            cohorts[7],
            cohorts[4],
            cohorts[2],
            cohorts[8],
            cohorts[1],
            cohorts[6],
            cohorts[0],
            cohorts[3],
            cohorts[5]
        ).sortedWith(InterpretedCohortComparator())

        val cohortIterator = cohortList.iterator()
        cohorts.forEach { assertThat(cohortIterator.next()).isEqualTo(it) }
    }

    @Test
    fun `Should place cohorts with open slots before those without`() {
        assertExpectedOrder(listOf(cohort, cohort.copy(hasSlotsAvailable = false)))
    }

    @Test
    fun `Should place cohorts from requesting source before those from other sources or source null`() {
        val cohort = cohort.copy(source = TrialSource.EXAMPLE)
        assertExpectedOrder(listOf(cohort, cohort.copy(source = TrialSource.LKO)), TrialSource.EXAMPLE)
        assertExpectedOrder(listOf(cohort, cohort.copy(source = null)), TrialSource.EXAMPLE)
    }

    @Test
    fun `Should place cohorts with the highest number of molecular events first and without molecular events last`() {
        assertExpectedOrder(
            listOf(
                cohort.copy(molecularEvents = cohort.molecularEvents + "Event X"),
                cohort,
                cohort.copy(molecularEvents = emptySet())
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

    private fun create(trialId: String, cohort: String?, hasSlotsAvailable: Boolean, vararg molecularEvents: String): InterpretedCohort {
        return InterpretedCohortTestFactory.interpretedCohort(
            trialId = trialId,
            acronym = "",
            molecularEvents = setOf(*molecularEvents),
            cohort = cohort,
            isPotentiallyEligible = false,
            isOpen = false,
            hasSlotsAvailable = hasSlotsAvailable
        )
    }
}