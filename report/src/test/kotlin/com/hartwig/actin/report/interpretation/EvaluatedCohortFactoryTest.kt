package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory.createTestMetadata
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory.create
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
        assertThat(trial1cohortA.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortA.isOpen).isTrue
        assertThat(trial1cohortA.hasSlotsAvailable).isFalse
        assertThat(trial1cohortA.warnings).isEmpty()
        assertThat(trial1cohortA.fails).isNotEmpty()

        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        assertThat(trial1cohortB.molecularEvents).isEmpty()
        assertThat(trial1cohortB.isPotentiallyEligible).isTrue
        assertThat(trial1cohortB.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortB.isOpen).isTrue
        assertThat(trial1cohortB.hasSlotsAvailable).isTrue
        assertThat(trial1cohortB.warnings).isEmpty()
        assertThat(trial1cohortB.fails).isNotEmpty()

        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        assertThat(trial1cohortC.molecularEvents).isEmpty()
        assertThat(trial1cohortC.isPotentiallyEligible).isFalse
        assertThat(trial1cohortC.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortC.isOpen).isFalse
        assertThat(trial1cohortC.hasSlotsAvailable).isFalse
        assertThat(trial1cohortC.warnings).isEmpty()
        assertThat(trial1cohortC.fails).isNotEmpty

        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        assertThat(trial2cohortA.molecularEvents).isNotEmpty
        assertThat(trial2cohortA.molecularEvents).containsExactly("MSI")
        assertThat(trial2cohortA.isPotentiallyEligible).isTrue
        assertThat(trial2cohortA.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial2cohortA.isOpen).isTrue
        assertThat(trial2cohortA.hasSlotsAvailable).isFalse
        assertThat(trial2cohortA.warnings).isEmpty()
        assertThat(trial2cohortA.fails).isEmpty()

        val trial2cohortB = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort B")
        assertThat(trial2cohortB.molecularEvents).isEmpty()
        assertThat(trial2cohortB.isPotentiallyEligible).isFalse
        assertThat(trial2cohortB.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial2cohortB.isOpen).isTrue
        assertThat(trial2cohortB.hasSlotsAvailable).isTrue
        assertThat(trial2cohortB.warnings).isEmpty()
        assertThat(trial2cohortB.fails).isNotEmpty
    }

    @Test
    fun `Should evaluate trials without cohort`() {
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

    @Test
    fun `Should correctly handle isMissingGenesForSufficientEvaluation flag`() {
        val cohortAEvaluation = createEvaluation(
            EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X,
            listOf("EGFR", "ALK", "ROS1"),
            EvaluationResult.UNDETERMINED,
            true
        )
        val cohortBEvaluation =
            createEvaluation(EligibilityRule.IS_AT_LEAST_X_YEARS_OLD, emptyList(), EvaluationResult.PASS, false)

        val cohorts = listOf(
            CohortMatch(
                metadata = createTestMetadata("A", true, true),
                isPotentiallyEligible = true,
                evaluations = cohortAEvaluation
            ),
            CohortMatch(
                metadata = createTestMetadata("B", true, true),
                isPotentiallyEligible = true,
                evaluations = cohortBEvaluation
            )
        )

        val trialMatch = TrialMatch(
            identification = TrialIdentification(
                trialId = "Test Trial 1",
                open = true,
                acronym = "TEST-1",
                title = "Example test trial 1",
                nctId = "NCT00000010",
                phase = TrialPhase.PHASE_1
            ),
            isPotentiallyEligible = true,
            evaluations = emptyMap(),
            cohorts = cohorts
        )

        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch))

        val evaluatedCohorts = create(treatmentMatch, false)
        val cohortA = findByAcronymAndCohort(evaluatedCohorts, "TEST-1", "Cohort A")
        val cohortB = findByAcronymAndCohort(evaluatedCohorts, "TEST-1", "Cohort B")
        assertThat(cohortA.isMissingGenesForSufficientEvaluation).isTrue()
        assertThat(cohortB.isMissingGenesForSufficientEvaluation).isFalse()
    }

    private fun findByAcronymAndCohort(
        evaluatedCohorts: List<EvaluatedCohort>, acronymToFind: String, cohortToFind: String?
    ): EvaluatedCohort {
        return evaluatedCohorts.first { it.acronym == acronymToFind && it.cohort == cohortToFind }
    }

    private fun createEvaluation(
        eligibilityRule: EligibilityRule, parameters: List<Any>, result: EvaluationResult, isMissingGenesForEvaluation: Boolean
    ): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(references = emptySet(), EligibilityFunction(eligibilityRule, parameters)) to Evaluation(
                result = result,
                recoverable = false,
                failGeneralMessages = emptySet(),
                inclusionMolecularEvents = emptySet(),
                isMissingGenesForSufficientEvaluation = isMissingGenesForEvaluation
            )
        )
    }
}