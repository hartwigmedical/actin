package com.hartwig.actin.report.interpretation

import com.google.common.collect.Lists
import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.function.Consumer

class EvaluatedCohortComparatorTest {
    @Test
    fun canSortEvaluatedCohorts() {
        val cohorts = Arrays.asList(
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
        val cohortList: List<EvaluatedCohort> = Lists.newArrayList(
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
        cohortList.sort(EvaluatedCohortComparator())
        val cohortIterator = cohortList.iterator()
        cohorts.forEach(Consumer { cohort: EvaluatedCohort? -> Assert.assertEquals(cohort, cohortIterator.next()) })
    }

    companion object {
        private fun create(
            trialId: String, cohort: String?, hasSlotsAvailable: Boolean,
            vararg molecularEvents: String
        ): EvaluatedCohort {
            val molecularEventSet: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
            if (molecularEvents.size > 0) {
                molecularEventSet.addAll(Lists.newArrayList(*molecularEvents))
            }
            return ImmutableEvaluatedCohort.builder()
                .trialId(trialId)
                .acronym(Strings.EMPTY)
                .molecularEvents(molecularEventSet)
                .cohort(cohort)
                .isPotentiallyEligible(false)
                .isOpen(false)
                .hasSlotsAvailable(hasSlotsAvailable)
                .build()
        }
    }
}