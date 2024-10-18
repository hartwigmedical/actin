package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.report.interpretation.EvaluatedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActinTrialContentFunctionsTest {
    private val cohort1 =
        EvaluatedCohort("trial1", "T1", "cohort1", setOf("MSI"), true, emptyList(), true, false, setOf("warning1"), emptySet())
    private val cohort2 =
        EvaluatedCohort("trial1", "T1", "cohort2", emptySet(), true, emptyList(), true, true, setOf("warning1", "warning2"), emptySet())

    @Test
    fun `Should group common warnings for multiple cohorts in trial`() {

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not group warnings for trial with single cohort`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1), EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common warnings`() {
        val cohorts = listOf(cohort1, cohort2.copy(warnings = setOf("warning2")))

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
            cohort1.copy(warnings = emptySet(), fails = setOf("failure1")),
            cohort2.copy(warnings = emptySet(), fails = setOf("failure1", "failure2"))
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, EvaluatedCohort::fails)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "failure1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "failure2"), false)
            )
        )
    }

    @Test
    fun `Should de-emphasize content for common messages when all cohorts are unavailable`() {
        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), EvaluatedCohort::warnings)
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(false, true, false))

        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2.copy(isOpen = false)), EvaluatedCohort::warnings)
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))

        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2.copy(hasSlotsAvailable = false)), EvaluatedCohort::warnings
            ).map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))
    }

    @Test
    fun `Should group common molecular events for multiple cohorts in trial`() {
        val cohort3 = cohort1.copy(cohort = "cohort3")
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort3), EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "MSI", "warning1"), true),
                ContentDefinition(listOf("cohort1", "", ""), true),
                ContentDefinition(listOf("cohort3", "", ""), true)
            )
        )
    }

    @Test
    fun `Should group molecular events for multiple cohorts in trial if molecular event is None for all cohorts`() {
        val noMolecularEvent1 = cohort1.copy(molecularEvents = emptySet())
        val noMolecularEvent2 = cohort1.copy(cohort = "cohort3", molecularEvents = emptySet())
        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(
                listOf(noMolecularEvent1, noMolecularEvent2),
                EvaluatedCohort::warnings
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "None", "warning1"), true),
                ContentDefinition(listOf("cohort1", "", ""), true),
                ContentDefinition(listOf("cohort3", "", ""), true)
            )
        )
    }

    @Test
    fun `Should not group molecular events for trials with single cohort`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1), EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common molecular events`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), EvaluatedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }
}