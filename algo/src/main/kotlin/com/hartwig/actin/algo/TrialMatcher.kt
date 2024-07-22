package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.sort.CohortMatchComparator
import com.hartwig.actin.algo.sort.TrialMatchComparator
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.sort.EligibilityComparator

class TrialMatcher(private val evaluationFunctionFactory: EvaluationFunctionFactory) {

    fun determineEligibility(patient: PatientRecord, trials: List<Trial>): List<TrialMatch> {
        return trials.map { trial ->
            val trialEvaluations = evaluateEligibility(patient, trial.generalEligibility)
            val passesAllTrialEvaluations = isPotentiallyEligible(trialEvaluations.values)
            val cohortMatches = trial.cohorts.filter { it.metadata.evaluable }.map { cohort ->
                val cohortEvaluations = evaluateEligibility(patient, cohort.eligibility)
                CohortMatch(
                    metadata = cohort.metadata,
                    isPotentiallyEligible = isPotentiallyEligible(cohortEvaluations.values) && passesAllTrialEvaluations,
                    evaluations = cohortEvaluations
                )
            }.sortedWith(CohortMatchComparator())

            val isEligible = passesAllTrialEvaluations && (trial.cohorts.isEmpty() || cohortMatches.any(CohortMatch::isPotentiallyEligible))
            TrialMatch(
                identification = trial.identification,
                isPotentiallyEligible = isEligible,
                evaluations = trialEvaluations,
                cohorts = cohortMatches
            )
        }.sortedWith(TrialMatchComparator())
    }

    private fun evaluateEligibility(patient: PatientRecord, eligibility: List<Eligibility>): Map<Eligibility, Evaluation> {
        return eligibility.sortedWith(EligibilityComparator()).associateWith {
            evaluationFunctionFactory.create(it.function).evaluate(patient)
        }
    }

    companion object {
        fun create(resources: RuleMappingResources): TrialMatcher {
            return TrialMatcher(EvaluationFunctionFactory.create(resources))
        }

        fun isPotentiallyEligible(evaluations: Iterable<Evaluation>): Boolean {
            return evaluations.none {
                !it.recoverable && (it.result == EvaluationResult.FAIL)
            }
        }
    }
}