package com.hartwig.actin.trial.sort

import com.hartwig.actin.trial.datamodel.Cohort
import com.hartwig.actin.trial.datamodel.CohortMetadata
import org.junit.Assert
import org.junit.Test

class CohortComparatorTest {
    @Test
    fun `Should sort cohorts`() {
        val cohort1 = withId("A")
        val cohort2 = withId("B")
        val cohorts = listOf(cohort2, cohort1).sortedWith(CohortComparator())
        Assert.assertEquals(cohort1, cohorts[0])
        Assert.assertEquals(cohort2, cohorts[1])
    }

    private fun withId(id: String): Cohort {
        return Cohort(
            metadata = CohortMetadata(
                cohortId = id,
                evaluable = true,
                open = true,
                slotsAvailable = true,
                blacklist = false,
                description = ""
            ),
            eligibility = emptyList()
        ) 
    }
}