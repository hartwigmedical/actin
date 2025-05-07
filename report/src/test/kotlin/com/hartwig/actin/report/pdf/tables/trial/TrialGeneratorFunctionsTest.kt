package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.util.Formats
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val APPLIES_TO_ALL_COHORTS = "${Formats.ITALIC_TEXT_MARKER}Applies to all cohorts below${Formats.ITALIC_TEXT_MARKER}"

class TrialGeneratorFunctionsTest {

    private val cohort1 = InterpretedCohort(
        trialId = "trial1",
        acronym = "T1",
        nctId = "nct01",
        title = "title1",
        phase = null,
        source = null,
        sourceId = null,
        locations = emptySet(),
        url = null,
        name = "cohort1",
        isOpen = true,
        hasSlotsAvailable = false,
        ignore = false,
        isEvaluable = false,
        molecularEvents = setOf("MSI"),
        isPotentiallyEligible = true,
        isMissingMolecularResultForEvaluation = false,
        warnings = setOf("warning1"),
        fails = emptySet()
    )

    private val cohort2 = cohort1.copy(
        nctId = "nct02",
        title = "title2",
        name = "cohort2",
        hasSlotsAvailable = true,
        warnings = setOf("warning1", "warning2"),
        source = TrialSource.LKO,
        sourceId = "123",
        molecularEvents = emptySet()
    )

    @Test
    fun `Should group common warnings for multiple cohorts in trial`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2),
                true,
                InterpretedCohort::warnings,
                false,
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "", ""), true),
                ContentDefinition(listOf("cohort2", "None", "", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not group warnings for trial with single cohort`() {
        assertThat(TrialGeneratorFunctions.contentForTrialCohortList(listOf(cohort1), true, InterpretedCohort::warnings, false)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should group common failures for multiple cohorts in trial`() {
        val cohorts = listOf(
            cohort1.copy(warnings = emptySet(), fails = setOf("failure1")),
            cohort2.copy(warnings = emptySet(), fails = setOf("failure1", "failure2"))
        )

        assertThat(TrialGeneratorFunctions.contentForTrialCohortList(cohorts, true, InterpretedCohort::fails, false)).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "", "", "failure1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "", ""), true),
                ContentDefinition(listOf("cohort2", "None", "", "failure2"), false)
            )
        )
    }

    @Test
    fun `Should de-emphasize content for common messages when all cohorts are unavailable`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(listOf(cohort1, cohort2), true, InterpretedCohort::warnings, false)
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(false, true, false))

        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2.copy(isOpen = false)),
                true,
                InterpretedCohort::warnings,
                false
            )
                .map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))

        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2.copy(hasSlotsAvailable = false)), true, InterpretedCohort::warnings, false
            ).map(ContentDefinition::deEmphasizeContent)
        ).isEqualTo(listOf(true, true, true))
    }

    @Test
    fun `Should group common molecular events for multiple cohorts in trial`() {
        val cohort3 = cohort1.copy(name = "cohort3")
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort3),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "MSI", "", "warning1"), true),
                ContentDefinition(listOf("cohort1", "", "", ""), true),
                ContentDefinition(listOf("cohort3", "", "", ""), true)
            )
        )
    }

    @Test
    fun `Should group molecular events for multiple cohorts in trial if molecular event is None for all cohorts`() {
        val noMolecularEvent1 = cohort1.copy(molecularEvents = emptySet())
        val noMolecularEvent2 = cohort1.copy(name = "cohort3", molecularEvents = emptySet())
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(noMolecularEvent1, noMolecularEvent2),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "None", "", "warning1"), true),
                ContentDefinition(listOf("cohort1", "", "", ""), true),
                ContentDefinition(listOf("cohort3", "", "", ""), true)
            )
        )
    }

    @Test
    fun `Should not group molecular events for trials with single cohort`() {
        assertThat(TrialGeneratorFunctions.contentForTrialCohortList(listOf(cohort1), true, InterpretedCohort::warnings, false)).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "", "warning1"), true),
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common molecular events`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1, cohort2),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "", "", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "", ""), true),
                ContentDefinition(listOf("cohort2", "None", "", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should group locations for cohorts if there are commonalities in locations between cohorts`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1.copy(locations = setOf("site1")), cohort2.copy(locations = setOf("site1"))),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf(APPLIES_TO_ALL_COHORTS, "", "site1", "warning1"), false),
                ContentDefinition(listOf("cohort1", "MSI", "", ""), true),
                ContentDefinition(listOf("cohort2", "None", "", "warning2"), false)
            )
        )
    }

    @Test
    fun `Should not create group row for multiple cohorts in trial with no common locations`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1.copy(locations = setOf("site1")), cohort2.copy(locations = setOf("site2"), warnings = emptySet())),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "site1", "warning1"), true),
                ContentDefinition(listOf("cohort2", "None", "site2", "None"), false)
            )
        )
    }

    @Test
    fun `Should put locations for cohorts on cohort row if there is only one cohort`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(cohort1.copy(locations = setOf("site1"), warnings = emptySet())),
                true,
                InterpretedCohort::warnings,
                false
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "site1", "None"), true)
            )
        )
    }

    @Test
    fun `Should put configuration at the end based on the cohorts fields`() {
        assertThat(
            TrialGeneratorFunctions.contentForTrialCohortList(
                listOf(
                    cohort1.copy(locations = setOf("site1")),
                    cohort2.copy(locations = setOf("site2"), ignore = true),
                    cohort1.copy(locations = setOf("site3"), isEvaluable = false, ignore = true),
                    cohort2.copy(locations = setOf("site4"), isEvaluable = true, ignore = true),
                    cohort1.copy(locations = setOf("site5"), isEvaluable = false),
                    ),
                false,
                InterpretedCohort::warnings,
                true
            )
        ).isEqualTo(
            listOf(
                ContentDefinition(listOf("cohort1", "MSI", "site1", "non-evaluable"), true),
                ContentDefinition(listOf("cohort2", "None", "site2", "ignored and non-evaluable"), false),
                ContentDefinition(listOf("cohort1", "MSI", "site3", "ignored and non-evaluable"), true),
                ContentDefinition(listOf("cohort2", "None", "site4", "ignored"), false),
                ContentDefinition(listOf("cohort1", "MSI", "site5", "non-evaluable"), true),
            )
        )
    }
}
