package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActinTrialContentFunctionsTest {

    @Test
    fun `Should group common warnings for multiple cohorts in trial`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, setOf("warning1"), emptySet()),
            EvaluatedCohort("trial1", "T1", "cohort2", emptySet(), true, true, true, setOf("warning1", "warning2"), emptySet()),
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not group warnings for trial with single cohort`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, setOf("warning1"), emptySet()),
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common warnings`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, setOf("warning1"), emptySet()),
            EvaluatedCohort("trial1", "T1", "cohort2", emptySet(), true, true, true, setOf("warning2"), emptySet()),
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should group common failures for multiple cohorts in trial`() {
        val cohorts = listOf(
            EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, true, false, emptySet(), setOf("failure1")),
            EvaluatedCohort("trial1", "T1", "cohort2", emptySet(), true, true, true, emptySet(), setOf("failure1", "failure2")),
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::fails)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "failure1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "failure2"), false)
            )
        )
    }
}