package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActinTrialGeneratorFunctionsTest {

    @Test
    fun `Should group common warnings for multiple cohorts in trial`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, setOf("warning1"), emptySet()),
            EvaluatedCohort("trial1", "T1", "cohort2", emptySet(), true, true, true, setOf("warning1", "warning2"), emptySet()),
        )

        assertThat(ActinTrialGeneratorFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("All cohorts", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "None"), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not group warnings for trial with single cohort`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, setOf("warning1"), emptySet()),
        )

        assertThat(ActinTrialGeneratorFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }
}