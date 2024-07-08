package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory.create
import com.hartwig.actin.trial.datamodel.TrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedCohortFactoryTest {
    @Test
    fun `Should create evaluated cohorts from minimal match`() {
        val cohorts = create(TestTreatmentMatchFactory.createMinimalTreatmentMatch(), false)
        assertThat(cohorts).isEmpty()
    }

    @Test
    fun `Should create evaluated cohorts from proper match`() {
        val cohorts = create(TestTreatmentMatchFactory.createProperTreatmentMatch(), false)
        assertThat(cohorts).hasSize(5)

        val trial1cohortA = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort A")
        assertThat(trial1cohortA.molecularEvents).isNotEmpty
        assertThat(trial1cohortA.molecularEvents).containsExactly("MSI")
        assertThat(trial1cohortA.isPotentiallyEligible).isTrue
        assertThat(trial1cohortA.isOpen).isTrue
        assertThat(trial1cohortA.hasSlotsAvailable).isFalse
        assertThat(trial1cohortA.warnings).isEmpty()
        assertThat(trial1cohortA.fails).isNotEmpty()

        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        assertThat(trial1cohortB.molecularEvents).isEmpty()
        assertThat(trial1cohortB.isPotentiallyEligible).isTrue
        assertThat(trial1cohortB.isOpen).isTrue
        assertThat(trial1cohortB.hasSlotsAvailable).isTrue
        assertThat(trial1cohortB.warnings).isEmpty()
        assertThat(trial1cohortB.fails).isNotEmpty()

        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        assertThat(trial1cohortC.molecularEvents).isEmpty()
        assertThat(trial1cohortC.isPotentiallyEligible).isFalse
        assertThat(trial1cohortC.isOpen).isFalse
        assertThat(trial1cohortC.hasSlotsAvailable).isFalse
        assertThat(trial1cohortC.warnings).isEmpty()
        assertThat(trial1cohortC.fails).isNotEmpty

        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        assertThat(trial2cohortA.molecularEvents).isNotEmpty
        assertThat(trial2cohortA.molecularEvents).containsExactly("MSI")
        assertThat(trial2cohortA.isPotentiallyEligible).isTrue
        assertThat(trial2cohortA.isOpen).isTrue
        assertThat(trial2cohortA.hasSlotsAvailable).isFalse
        assertThat(trial2cohortA.warnings).isEmpty()
        assertThat(trial2cohortA.fails).isEmpty()

        val trial2cohortB = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort B")
        assertThat(trial2cohortB.molecularEvents).isEmpty()
        assertThat(trial2cohortB.isPotentiallyEligible).isFalse
        assertThat(trial2cohortB.isOpen).isTrue
        assertThat(trial2cohortB.hasSlotsAvailable).isTrue
        assertThat(trial2cohortB.warnings).isEmpty()
        assertThat(trial2cohortB.fails).isNotEmpty
    }

    @Test
    fun `Can evaluate trials without cohort`() {
        val trialMatchWithoutCohort = TrialMatch(
            identification = TrialIdentification(
                trialId = "test",
                open = true,
                acronym = "test-1",
                title = "Example test trial 1",
                nctId = null
            ),
            isPotentiallyEligible = true,
            cohorts = emptyList(),
            evaluations = emptyMap()
        )

        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(trialMatches = listOf(trialMatchWithoutCohort))
        val cohorts = create(treatmentMatch, false)
        assertThat(cohorts).hasSize(1)
    }

    @Test
    fun `Should filter trials on SOC exhaustion and tumor type`() {
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val cohortsWithoutFiltering = create(treatmentMatch, false)
        assertThat(cohortsWithoutFiltering).hasSize(5)
        val cohortsWithFiltering = create(treatmentMatch, true)
        assertThat(cohortsWithFiltering).hasSize(2)
    }

    private fun findByAcronymAndCohort(
        evaluatedCohorts: List<EvaluatedCohort>, acronymToFind: String, cohortToFind: String?
    ): EvaluatedCohort {
        return evaluatedCohorts.first { it.acronym == acronymToFind && it.cohort == cohortToFind }
    }
}