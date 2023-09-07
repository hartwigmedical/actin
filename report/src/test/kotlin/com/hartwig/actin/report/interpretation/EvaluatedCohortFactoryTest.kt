package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory.create
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedCohortFactoryTest {
    @Test
    fun shouldCreateEvaluatedCohortsFromMinimalMatch() {
        val cohorts = create(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
        assertThat(cohorts.isEmpty()).isTrue
    }

    @Test
    fun shouldCreateEvaluatedCohortsFromProperMatch() {
        val cohorts = create(TestTreatmentMatchFactory.createProperTreatmentMatch())
        assertThat(cohorts).hasSize(5)

        val trial1cohortA = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort A")
        assertThat(trial1cohortA.molecularEvents.isEmpty()).isFalse
        assertThat(trial1cohortA.molecularEvents.contains("BRAF V600E")).isTrue
        assertThat(trial1cohortA.isPotentiallyEligible).isTrue
        assertThat(trial1cohortA.isOpen).isTrue
        assertThat(trial1cohortA.hasSlotsAvailable).isFalse
        assertThat(trial1cohortA.warnings.isEmpty()).isFalse
        assertThat(trial1cohortA.fails.isEmpty()).isTrue

        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        assertThat(trial1cohortB.molecularEvents.isEmpty()).isTrue
        assertThat(trial1cohortB.isPotentiallyEligible).isTrue
        assertThat(trial1cohortB.isOpen).isTrue
        assertThat(trial1cohortB.hasSlotsAvailable).isTrue
        assertThat(trial1cohortB.warnings.isEmpty()).isFalse
        assertThat(trial1cohortB.fails.isEmpty()).isTrue

        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        assertThat(trial1cohortC.molecularEvents.isEmpty()).isTrue
        assertThat(trial1cohortC.isPotentiallyEligible).isFalse
        assertThat(trial1cohortC.isOpen).isFalse
        assertThat(trial1cohortC.hasSlotsAvailable).isFalse
        assertThat(trial1cohortC.warnings.isEmpty()).isFalse
        assertThat(trial1cohortC.fails.isEmpty()).isFalse

        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        assertThat(trial2cohortA.molecularEvents.isEmpty()).isFalse
        assertThat(trial2cohortA.molecularEvents.contains("BRAF V600E")).isTrue
        assertThat(trial2cohortA.isPotentiallyEligible).isTrue
        assertThat(trial2cohortA.isOpen).isTrue
        assertThat(trial2cohortA.hasSlotsAvailable).isFalse
        assertThat(trial2cohortA.warnings.isEmpty()).isTrue
        assertThat(trial2cohortA.fails.isEmpty()).isTrue

        val trial2cohortB = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort B")
        assertThat(trial2cohortB.molecularEvents.isEmpty()).isTrue
        assertThat(trial2cohortB.isPotentiallyEligible).isFalse
        assertThat(trial2cohortB.isOpen).isTrue
        assertThat(trial2cohortB.hasSlotsAvailable).isTrue
        assertThat(trial2cohortB.warnings.isEmpty()).isTrue
        assertThat(trial2cohortB.fails.isEmpty()).isFalse
    }

    @Test
    fun canEvaluateTrialsWithoutCohort() {
        val trialMatchWithoutCohort: TrialMatch = ImmutableTrialMatch.builder()
            .identification(
                ImmutableTrialIdentification.builder()
                    .trialId("test")
                    .open(true)
                    .acronym("test-1")
                    .title("Example test trial 1")
                    .build()
            )
            .isPotentiallyEligible(true)
            .build()

        val treatmentMatch: TreatmentMatch = ImmutableTreatmentMatch.builder()
            .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
            .addTrialMatches(trialMatchWithoutCohort)
            .build()
        val cohorts = create(treatmentMatch)
        assertThat(cohorts).hasSize(1)
    }

    companion object {
        private fun findByAcronymAndCohort(
            evaluatedCohorts: List<EvaluatedCohort>, acronymToFind: String, cohortToFind: String?
        ): EvaluatedCohort {
            return evaluatedCohorts.first { it.acronym == acronymToFind && it.cohort == cohortToFind }
        }
    }
}