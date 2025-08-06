package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory.createTestCohortMetadata
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
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
        assertThat(trial1cohortA.molecularInclusionEvents).isNotEmpty
        assertThat(trial1cohortA.molecularInclusionEvents).containsExactly("MSI")
        assertThat(trial1cohortA.isPotentiallyEligible).isTrue
        assertThat(trial1cohortA.isMissingMolecularResultForEvaluation).isFalse()
        assertThat(trial1cohortA.isOpen).isTrue
        assertThat(trial1cohortA.hasSlotsAvailable).isFalse
        assertThat(trial1cohortA.ignore).isFalse
        assertThat(trial1cohortA.warnings).isEmpty()
        assertThat(trial1cohortA.fails).isNotEmpty()
        assertThat(trial1cohortA.source).isEqualTo(TrialSource.NKI)
        assertThat(trial1cohortA.locations).isEqualTo(setOf("Antoni van Leeuwenhoek"))

        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        assertThat(trial1cohortB.molecularInclusionEvents).containsExactly("HER2 amp")
        assertThat(trial1cohortB.isPotentiallyEligible).isTrue
        assertThat(trial1cohortB.isMissingMolecularResultForEvaluation).isFalse()
        assertThat(trial1cohortB.isOpen).isTrue
        assertThat(trial1cohortB.hasSlotsAvailable).isTrue
        assertThat(trial1cohortB.ignore).isFalse
        assertThat(trial1cohortB.warnings).isEmpty()
        assertThat(trial1cohortB.fails).isNotEmpty()
        assertThat(trial1cohortA.source).isEqualTo(TrialSource.NKI)
        assertThat(trial1cohortA.locations).isEqualTo(setOf("Antoni van Leeuwenhoek"))

        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        assertThat(trial1cohortC.molecularInclusionEvents).isEmpty()
        assertThat(trial1cohortC.isPotentiallyEligible).isFalse
        assertThat(trial1cohortC.isMissingMolecularResultForEvaluation).isFalse()
        assertThat(trial1cohortC.isOpen).isFalse
        assertThat(trial1cohortC.hasSlotsAvailable).isFalse
        assertThat(trial1cohortC.ignore).isFalse
        assertThat(trial1cohortC.warnings).isEmpty()
        assertThat(trial1cohortC.fails).isNotEmpty
        assertThat(trial1cohortA.source).isEqualTo(TrialSource.NKI)
        assertThat(trial1cohortA.locations).isEqualTo(setOf("Antoni van Leeuwenhoek"))

        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        assertThat(trial2cohortA.molecularInclusionEvents).isNotEmpty
        assertThat(trial2cohortA.molecularInclusionEvents).containsExactly("MSI")
        assertThat(trial2cohortA.isPotentiallyEligible).isTrue
        assertThat(trial2cohortA.isMissingMolecularResultForEvaluation).isFalse()
        assertThat(trial2cohortA.isOpen).isTrue
        assertThat(trial2cohortA.hasSlotsAvailable).isFalse
        assertThat(trial2cohortA.ignore).isFalse
        assertThat(trial2cohortA.warnings).isEmpty()
        assertThat(trial2cohortA.fails).isEmpty()
        assertThat(trial2cohortA.source).isEqualTo(TrialSource.NKI)
        assertThat(trial2cohortA.locations).containsExactly("Antoni van Leeuwenhoek")
    }

    @Test
    fun `Should create non evaluable cohorts from proper match`() {
        val nonEvaluableCohorts = createNonEvaluableCohorts(TestTreatmentMatchFactory.createProperTreatmentMatch())
        assertThat(nonEvaluableCohorts).hasSize(1)

        val trial2cohortB = findByAcronymAndCohort(nonEvaluableCohorts, "TEST-2", "Cohort B")
        assertThat(trial2cohortB.molecularInclusionEvents).isEmpty()
        assertThat(trial2cohortB.isPotentiallyEligible).isFalse
        assertThat(trial2cohortB.isMissingMolecularResultForEvaluation).isFalse()
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
                nctId = null,
                phase = null,
                source = null,
                sourceId = null,
                locations = emptySet(),
                url = null
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
    fun `Should correctly handle isMissingMolecularResultForEvaluation flag on cohort level`() {
        val cohortAEvaluation = createEvaluation(RULE_1, listOf("EGFR", "ALK", "ROS1"), EvaluationResult.UNDETERMINED, true)
        val cohortBEvaluation = createEvaluation(RULE_2, emptyList(), EvaluationResult.PASS, false)

        val cohorts = listOf(createCohortMatch("A", cohortAEvaluation), createCohortMatch("B", cohortBEvaluation))
        val trialMatch = createTrialMatch(cohorts, emptyMap())
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch))

        val evaluatedCohorts = createEvaluableCohorts(treatmentMatch, false)
        val cohortA = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort A")
        val cohortB = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort B")
        assertThat(cohortA.isMissingMolecularResultForEvaluation).isTrue()
        assertThat(cohortB.isMissingMolecularResultForEvaluation).isFalse()
    }

    @Test
    fun `Should correctly set isMissingMolecularResultForEvaluation to true for cohorts if true for a trial evaluation`() {
        val cohortAEvaluation = createEvaluation(RULE_1, listOf("EGFR"), EvaluationResult.PASS, false)
        val cohortBEvaluation = createEvaluation(RULE_2, emptyList(), EvaluationResult.PASS, false)
        val trialEvaluation = createEvaluation(RULE_1, listOf("ALK, ROS1"), EvaluationResult.UNDETERMINED, true)

        val cohorts = listOf(createCohortMatch("A", cohortAEvaluation), createCohortMatch("B", cohortBEvaluation))
        val trialMatch = createTrialMatch(cohorts, trialEvaluation)
        val treatmentMatch = TestTreatmentMatchFactory.createProperTreatmentMatch().copy(trialMatches = listOf(trialMatch))

        val evaluatedCohorts = createEvaluableCohorts(treatmentMatch, false)
        val cohortA = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort A")
        val cohortB = findByAcronymAndCohort(evaluatedCohorts, TRIAL_NAME, "Cohort B")
        assertThat(cohortA.isMissingMolecularResultForEvaluation).isTrue()
        assertThat(cohortB.isMissingMolecularResultForEvaluation).isTrue()
    }

    private fun findByAcronymAndCohort(
        cohorts: List<InterpretedCohort>, acronymToFind: String, cohortToFind: String?
    ): InterpretedCohort {
        return cohorts.first { it.acronym == acronymToFind && it.name == cohortToFind }
    }

    private fun createEvaluation(
        eligibilityRule: EligibilityRule, parameters: List<Any>, result: EvaluationResult, isMissingMolecularResultForEvaluation: Boolean
    ): Map<Eligibility, Evaluation> {
        return mapOf(
            Eligibility(references = emptySet(), EligibilityFunction(eligibilityRule, parameters)) to Evaluation(
                result = result,
                recoverable = false,
                failMessages = emptySet(),
                inclusionMolecularEvents = emptySet(),
                isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
            )
        )
    }

    private fun createCohortMatch(name: String, evaluation: Map<Eligibility, Evaluation>): CohortMatch {
        return CohortMatch(
            metadata = createTestCohortMetadata(name, open = true, evaluable = true, slotsAvailable = true, ignore = false),
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
                phase = TrialPhase.PHASE_1,
                source = null,
                sourceId = null,
                locations = emptySet(),
                url = null
            ),
            isPotentiallyEligible = true,
            evaluations = evaluation,
            cohorts = cohorts,
            nonEvaluableCohorts = emptyList()
        )
    }
}