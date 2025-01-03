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
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory.createEvaluableCohorts
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory.createNonEvaluableCohorts
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val RULE_1 = EligibilityRule.ACTIVATING_MUTATION_IN_ANY_GENES_X
private val RULE_2 = EligibilityRule.IS_AT_LEAST_X_YEARS_OLD
private const val TRIAL_NAME = "TEST-1"

class InterpretedCohortFactoryTest {

    @Test
    fun `Should create evaluated cohorts from minimal match`() {
        val cohorts = createEvaluableCohorts(TestTreatmentMatchFactory.createMinimalTreatmentMatch(), false)
        assertThat(cohorts).isEmpty()
    }

    @Test
    fun `Should create non evaluable cohorts from minimal match`() {
        val nonEvaluableCohorts = createNonEvaluableCohorts(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
        assertThat(nonEvaluableCohorts).isEmpty()
    }

    @Test
    fun `Should create evaluated cohorts from proper match`() {
        val cohorts = createEvaluableCohorts(TestTreatmentMatchFactory.createProperTreatmentMatch(), false)
        assertThat(cohorts).hasSize(4)

        val trial1cohortA = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort A")
        assertThat(trial1cohortA.molecularEvents).isNotEmpty
        assertThat(trial1cohortA.molecularEvents).containsExactly("MSI")
        assertThat(trial1cohortA.isPotentiallyEligible).isTrue
        assertThat(trial1cohortA.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortA.isOpen).isTrue
        assertThat(trial1cohortA.hasSlotsAvailable).isFalse
        assertThat(trial1cohortA.ignore).isFalse
        assertThat(trial1cohortA.warnings).isEmpty()
        assertThat(trial1cohortA.fails).isNotEmpty()

        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        assertThat(trial1cohortB.molecularEvents).isEmpty()
        assertThat(trial1cohortB.isPotentiallyEligible).isTrue
        assertThat(trial1cohortB.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortB.isOpen).isTrue
        assertThat(trial1cohortB.hasSlotsAvailable).isTrue
        assertThat(trial1cohortB.ignore).isFalse
        assertThat(trial1cohortB.warnings).isEmpty()
        assertThat(trial1cohortB.fails).isNotEmpty()

        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        assertThat(trial1cohortC.molecularEvents).isEmpty()
        assertThat(trial1cohortC.isPotentiallyEligible).isFalse
        assertThat(trial1cohortC.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial1cohortC.isOpen).isFalse
        assertThat(trial1cohortC.hasSlotsAvailable).isFalse
        assertThat(trial1cohortC.ignore).isFalse
        assertThat(trial1cohortC.warnings).isEmpty()
        assertThat(trial1cohortC.fails).isNotEmpty

        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        assertThat(trial2cohortA.molecularEvents).isNotEmpty
        assertThat(trial2cohortA.molecularEvents).containsExactly("MSI")
        assertThat(trial2cohortA.isPotentiallyEligible).isTrue
        assertThat(trial2cohortA.isMissingGenesForSufficientEvaluation).isFalse()
        assertThat(trial2cohortA.isOpen).isTrue
        assertThat(trial2cohortA.hasSlotsAvailable).isFalse
        assertThat(trial2cohortA.ignore).isFalse
        assertThat(trial2cohortA.warnings).isEmpty()
        assertThat(trial2cohortA.fails).isEmpty()
    }

    @Test
    fun `Should create non evaluable cohorts from proper match`() {
        val nonEvaluableCohorts = createNonEvaluableCohorts(TestTreatmentMatchFactory.createProperTreatmentMatch())
        assertThat(nonEvaluableCohorts).hasSize(1)

        val trial2cohortB = findByAcronymAndCohort(nonEvaluableCohorts, "TEST-2", "Cohort B")
        assertThat(trial2cohortB.molecularEvents).isEmpty()
        assertThat(trial2cohortB.isPotentiallyEligible).isFalse
        assertThat(trial2cohortB.isMissingGenesForSufficientEvaluation).isEqualTo(null)
        assertThat(trial2cohortB.isOpen).isTrue
        assertThat(trial2cohortB.hasSlotsAvailable).isTrue
        assertThat(trial2cohortB.ignore).isFalse
        assertThat(trial2cohortB.warnings).isEmpty()
        assertThat(trial2cohortB.fails).isEmpty()
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
            nonEvaluableCohorts = emptyList(),
            evaluations = emptyMap()
        )

        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(trialMatches = listOf(trialMatchWithoutCohort))
        val cohorts = createEvaluableCohorts(treatmentMatch, false)
        assertThat(cohorts).hasSize(1)
    }

    @Test
    fun `Should filter trials on SOC exhaustion and tumor type`() {
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch()
        val cohortsWithoutFiltering = createEvaluableCohorts(treatmentMatch, false)
        assertThat(cohortsWithoutFiltering).hasSize(4)
        val cohortsWithFiltering = createEvaluableCohorts(treatmentMatch, true)
        assertThat(cohortsWithFiltering).hasSize(1)
    }

    @Test
    fun `Should correctly handle isMissingGenesForSufficientEvaluation flag on cohort level`() {
        val cohortAEvaluation = createEvaluation(RULE_1, listOf("EGFR", "ALK", "ROS1"), EvaluationResult.UNDETERMINED, true)
        val cohortBEvaluation = createEvaluation(RULE_2, emptyList(), EvaluationResult.PASS, false)

        val cohorts = listOf(createCohortMatch("A", cohortAEvaluation), createCohortMatch("B", cohortBEvaluation))
        val trialMatch = createTrialMatch(cohorts, emptyMap())
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch))

        val evaluatedCohorts = createEvaluableCohorts(treatmentMatch, false)
        val cohortA = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort A")
        val cohortB = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort B")
        assertThat(cohortA.isMissingGenesForSufficientEvaluation).isTrue()
        assertThat(cohortB.isMissingGenesForSufficientEvaluation).isFalse()
    }

    @Test
    fun `Should correctly set isMissingGenesForSufficientEvaluation to true for cohorts if true for a trial evaluation`() {
        val cohortAEvaluation = createEvaluation(RULE_1, listOf("EGFR"), EvaluationResult.PASS, false)
        val cohortBEvaluation = createEvaluation(RULE_2, emptyList(), EvaluationResult.PASS, false)
        val trialEvaluation = createEvaluation(RULE_1, listOf("ALK, ROS1"), EvaluationResult.UNDETERMINED, true)

        val cohorts = listOf(createCohortMatch("A", cohortAEvaluation), createCohortMatch("B", cohortBEvaluation))
        val trialMatch = createTrialMatch(cohorts, trialEvaluation)
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch))

        val evaluatedCohorts = createEvaluableCohorts(treatmentMatch, false)
        val cohortA = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort A")
        val cohortB = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort B")
        assertThat(cohortA.isMissingGenesForSufficientEvaluation).isTrue()
        assertThat(cohortB.isMissingGenesForSufficientEvaluation).isTrue()
    }

    private fun findByAcronymAndCohort(
        cohorts: List<InterpretedCohort>, acronymToFind: String, cohortToFind: String?
    ): InterpretedCohort {
        return cohorts.first { it.acronym == acronymToFind && it.name == cohortToFind }
    }

    private fun createEvaluation(
        eligibilityRule: EligibilityRule, parameters: List<Any>, result: EvaluationResult, isMissingGenesForEvaluation: Boolean
    ): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(references = emptySet(), EligibilityFunction(eligibilityRule, parameters)) to Evaluation(
                result = result,
                recoverable = false,
                failMessages = emptySet(),
                inclusionMolecularEvents = emptySet(),
                isMissingGenesForSufficientEvaluation = isMissingGenesForEvaluation
            )
        )
    }

    private fun createCohortMatch(name: String, evaluation: Map<Eligibility, Evaluation>): CohortMatch {
        return CohortMatch(
            metadata = createTestMetadata(name, open = true, evaluable = true, slotsAvailable = true, ignore = false),
            isPotentiallyEligible = true,
            evaluations = evaluation
        )
    }

    private fun createTrialMatch(cohorts: List<CohortMatch>, evaluation: Map<Eligibility, Evaluation>): TrialMatch {
        return TrialMatch(
            identification = TrialIdentification(
                trialId = TRIAL_NAME,
                open = true,
                acronym = TRIAL_NAME,
                title = "Example test trial 1",
                nctId = "NCT00000010",
                phase = TrialPhase.PHASE_1
            ),
            isPotentiallyEligible = true,
            evaluations = evaluation,
            cohorts = cohorts,
            nonEvaluableCohorts = emptyList()
        )
    }
}