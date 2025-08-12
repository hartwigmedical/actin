package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortAvailability
import com.hartwig.actin.datamodel.trial.CohortMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortComparatorTest {

    @Test
    fun `Should sort cohorts`() {
        val cohort1 = withId("A")
        val cohort2 = withId("B")
        val cohorts = listOf(cohort2, cohort1).sortedWith(CohortComparator())
        assertThat(cohorts).containsExactly(cohort1, cohort2)
    }

    private fun withId(id: String): Cohort {
        return Cohort(
            metadata = CohortMetadata(
                cohortId = id,
                evaluable = true,
                cohortAvailability = CohortAvailability(
                    true,
                    slotsAvailable = true
                ),
                ignore = false,
                description = ""
            ),
            eligibility = emptyList()
        )
    }
}