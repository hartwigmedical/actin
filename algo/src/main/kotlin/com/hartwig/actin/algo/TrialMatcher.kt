package com.hartwig.actin.algo

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableCohortMatch
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory
import com.hartwig.actin.algo.sort.CohortMatchComparator
import com.hartwig.actin.algo.sort.TrialMatchComparator
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.sort.EligibilityComparator

class TrialMatcher internal constructor(private val evaluationFunctionFactory: EvaluationFunctionFactory) {
    fun determineEligibility(patient: PatientRecord, trials: List<Trial>): List<TrialMatch> {
        val trialMatches: MutableList<TrialMatch> = mutableListOf()
        for (trial in trials) {
            val trialEvaluations = evaluateEligibility(patient, trial.generalEligibility())
            val passesAllTrialEvaluations = isPotentiallyEligible(trialEvaluations.values)
            var hasEligibleCohort = false
            val cohortMatches: List<CohortMatch> = trial.cohorts().filter { it.metadata().evaluable() }.map { cohort ->
                val cohortEvaluations = evaluateEligibility(patient, cohort.eligibility())
                val isPotentiallyEligible = isPotentiallyEligible(cohortEvaluations.values)
                if (isPotentiallyEligible) {
                    hasEligibleCohort = true
                }
                ImmutableCohortMatch.builder().metadata(cohort.metadata())
                    .isPotentiallyEligible(isPotentiallyEligible && passesAllTrialEvaluations).evaluations(cohortEvaluations).build()
            }.sortedWith(CohortMatchComparator())

            val isEligible = passesAllTrialEvaluations && (trial.cohorts().isEmpty() || hasEligibleCohort)
            trialMatches.add(
                ImmutableTrialMatch.builder().identification(trial.identification()).isPotentiallyEligible(isEligible)
                    .evaluations(trialEvaluations).cohorts(cohortMatches).build()
            )
        }
        return trialMatches.sortedWith(TrialMatchComparator())
    }

    private fun evaluateEligibility(patient: PatientRecord, eligibility: List<Eligibility>): Map<Eligibility, Evaluation> {
        return eligibility.sortedWith(EligibilityComparator()).associateWith {
            evaluationFunctionFactory.create(it.function()).evaluate(patient)
        }
    }

    companion object {
        fun create(doidModel: DoidModel, referenceDateProvider: ReferenceDateProvider): TrialMatcher {
            return TrialMatcher(EvaluationFunctionFactory.create(doidModel, referenceDateProvider))
        }

        fun isPotentiallyEligible(evaluations: Iterable<Evaluation>): Boolean {
            return evaluations.none {
                !it.recoverable() && (it.result() == EvaluationResult.FAIL || it.result() == EvaluationResult.NOT_IMPLEMENTED)
            }
        }
    }
}