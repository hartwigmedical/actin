package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.trial.TrialLocation
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ActinTrialGeneratorFunctionsTest {

    private val cohort1 = InterpretedCohortTestFactory.interpretedCohort(
        "trial1",
        "T1",
        isPotentiallyEligible = true,
        isOpen = true,
        hasSlotsAvailable = false,
        molecularEvents = setOf("MSI"),
        cohort = "Cohort A"
    )
    private val cohort2 = cohort1.copy("trial2", name = "Cohort B", source = TrialSource.NKI)
    private val cohort3 =
        cohort1.copy("trial3", name = "Cohort C", source = TrialSource.LKO, locations = listOf(TrialLocation(1, "Erasmus")))

    @Test
    fun `Should return correct table title based on source`() {
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart(null)).isEqualTo("Trials")
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart("")).isEqualTo(" trials")
        assertThat(ActinTrialGeneratorFunctions.createTableTitleStart("1")).isEqualTo("1 trials")
    }

    @Test
    fun `Should return all cohorts in the own list when source is null`() {
        val (own, others) = ActinTrialGeneratorFunctions.partitionBaseOnLocation(listOf(cohort1, cohort2, cohort3), null)
        assertThat(own).size().isEqualTo(3)
        assertThat(others).isEmpty()
    }

    @Test
    fun `Should return all cohorts in the own list when source is EMC`() {
        val (own, others) = ActinTrialGeneratorFunctions.partitionBaseOnLocation(listOf(cohort1, cohort2, cohort3), TrialSource.EMC)
        assertThat(own).size().isEqualTo(3)
        assertThat(others).isEmpty()
    }

    @Test
    fun `Should return all cohorts in the own list when source there are no other sources `() {
        val (own, others) = ActinTrialGeneratorFunctions.partitionBaseOnLocation(
            listOf(cohort1, cohort2, cohort3.copy(source = null)),
            TrialSource.NKI
        )
        assertThat(own).size().isEqualTo(3)
        assertThat(others).isEmpty()
    }

    @Test
    fun `Should return all cohorts in the own list when source is NKI`() {
        val (own, others) = ActinTrialGeneratorFunctions.partitionBaseOnLocation(listOf(cohort1, cohort2, cohort3), TrialSource.NKI)
        assertThat(own).size().isEqualTo(2)
        assertThat(others).size().isEqualTo(1)
    }
}