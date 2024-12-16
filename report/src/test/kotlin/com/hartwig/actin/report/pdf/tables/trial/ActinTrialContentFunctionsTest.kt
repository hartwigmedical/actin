package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActinTrialContentFunctionsTest {

    private val cohort1 = InterpretedCohort(
        "trial1",
        "T1",
        name = "cohort1",
        isOpen = true,
        hasSlotsAvailable = false,
        molecularEvents = setOf("MSI"),
        isPotentiallyEligible = true,
        warnings = setOf("warning1"),
        fails = emptySet()
    )
    private val cohort2 =
        InterpretedCohort(
            "trial1",
            "T1",
            name = "cohort2",
            isOpen = true,
            hasSlotsAvailable = true,
            isPotentiallyEligible = true,
            warnings = setOf("warning1", "warning2"),
            fails = emptySet(),
            source = TrialSource.LKO,
            locations = listOf("Erasmus", "NKI")
        )

    @Test
    fun `Should group common warnings for multiple cohorts in trial`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), InterpretedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not group warnings for trial with single cohort`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1), InterpretedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common warnings`() {
        val cohorts = listOf(cohort1, cohort2.copy(warnings = setOf("warning2")))

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, InterpretedCohort::warnings)).isEqualTo(
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

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, InterpretedCohort::fails)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "failure1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "failure2"), false)
            )
        )
    }

    @Test
    fun `Should group common failures for multiple cohorts in trial showing location`() {
        val cohorts = listOf(
            cohort1.copy(warnings = emptySet(), fails = setOf("failure1")),
            cohort2.copy(warnings = emptySet(), fails = setOf("failure1", "failure2"))
        )

        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(cohorts, InterpretedCohort::fails, true, true)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "", "failure1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "", ""), true),
                ContentDefinition(listOf("cohort2", "None", "Erasmus\nNKI", "failure2"), false)
            )
        )
    }

    @Test
    fun `Should de-emphasize content for common messages when all cohorts are unavailable`() {
        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), InterpretedCohort::warnings)
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(false, true, false))

        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2.copy(isOpen = false)), InterpretedCohort::warnings)
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))

        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2.copy(hasSlotsAvailable = false)), InterpretedCohort::warnings
            ).map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))
    }

    @Test
    fun `Should group common molecular events for multiple cohorts in trial`() {
        val cohort3 = cohort1.copy(name = "cohort3")
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort3), InterpretedCohort::warnings)).isEqualTo(
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
        val noMolecularEvent2 = cohort1.copy(name = "cohort3", molecularEvents = emptySet())
        assertThat(
            ActinTrialContentFunctions.contentForTrialCohortList(
                listOf(noMolecularEvent1, noMolecularEvent2),
                InterpretedCohort::warnings
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
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1), InterpretedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common molecular events`() {
        assertThat(ActinTrialContentFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), InterpretedCohort::warnings)).isEqualTo(
            listOf(
                ContentDefinition(listOf("Applies to all cohorts below", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", ""), true),
                ContentDefinition(listOf("cohort2", "None", "warning2"), false)
            )
        )
    }
}