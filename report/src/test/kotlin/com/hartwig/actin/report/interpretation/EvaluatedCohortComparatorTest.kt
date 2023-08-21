package com.hartwig.actin.report.interpretation

import org.junit.Assert
import org.junit.Test

class EvaluatedCohortComparatorTest {
    @Test
    fun canSortEvaluatedCohorts() {
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
        val cohortList: List<EvaluatedCohort> = listOf(
            cohorts[7],
            cohorts[4],
            cohorts[2],
            cohorts[8],
            cohorts[1],
            cohorts[6],
            cohorts[0],
            cohorts[3],
            cohorts[5]
        )
        cohortList.sortedWith(EvaluatedCohortComparator())
        val cohortIterator = cohortList.iterator()
        cohorts.forEach { Assert.assertEquals(it, cohortIterator.next()) }
    }

    companion object {
        private fun create(trialId: String, cohort: String?, hasSlotsAvailable: Boolean, vararg molecularEvents: String): EvaluatedCohort {
            return EvaluatedCohortTestFactory.evaluatedCohort(
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
}