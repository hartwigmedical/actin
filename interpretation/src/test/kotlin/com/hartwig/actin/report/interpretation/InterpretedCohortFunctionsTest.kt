package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.trial.TrialSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InterpretedCohortFunctionsTest {

    @Test
    fun `Should return true when source matches requestingSource`() {
        assertThat(
            InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(
                source = TrialSource.EXAMPLE,
                locations = emptySet(),
                requestingSource = TrialSource.EXAMPLE
            )
        ).isTrue()
    }

    @Test
    fun `Should return true when any location matches requestingSource`() {
        listOf(TrialSource.NKI, null).forEach {
            assertThat(
                InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(
                    source = it,
                    locations = setOf(TrialSource.NKI.description, TrialSource.EXAMPLE.description),
                    requestingSource = TrialSource.EXAMPLE
                )
            ).isTrue()
        }
    }

    @Test
    fun `Should return false when source and location do not match requestingSource`() {
        listOf(TrialSource.NKI, null).forEach {
            assertThat(
                InterpretedCohortFunctions.sourceOrLocationMatchesRequestingSource(
                    source = it,
                    locations = setOf(TrialSource.EMC.description),
                    requestingSource = TrialSource.EXAMPLE
                )
            ).isFalse()
        }
    }

}