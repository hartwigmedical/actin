package com.hartwig.actin.treatment.sort

import com.google.common.collect.Lists
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.ImmutableCohort
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class CohortComparatorTest {
    @Test
    fun canSortCohorts() {
        val cohort1 = withId("A")
        val cohort2 = withId("B")
        val cohorts: List<Cohort> = Lists.newArrayList(cohort2, cohort1)
        cohorts.sort(CohortComparator())
        Assert.assertEquals(cohort1, cohorts[0])
        Assert.assertEquals(cohort2, cohorts[1])
    }

    companion object {
        private fun withId(id: String): Cohort {
            return ImmutableCohort.builder()
                .metadata(
                    ImmutableCohortMetadata.builder()
                        .cohortId(id)
                        .evaluable(true)
                        .open(true)
                        .slotsAvailable(true)
                        .blacklist(false)
                        .description(Strings.EMPTY)
                        .build()
                )
                .build()
        }
    }
}