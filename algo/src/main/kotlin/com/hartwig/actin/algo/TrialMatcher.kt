package com.hartwig.actin.algo

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Lists
import com.google.common.collect.Maps
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

class TrialMatcher @VisibleForTesting internal constructor(private val evaluationFunctionFactory: EvaluationFunctionFactory) {
    fun determineEligibility(patient: PatientRecord, trials: MutableList<Trial?>): MutableList<TrialMatch?> {
        val trialMatches: MutableList<TrialMatch?>? = Lists.newArrayList()
        for (trial in trials) {
            val trialEvaluations = evaluateEligibility(patient, trial.generalEligibility())
            val passesAllTrialEvaluations = isPotentiallyEligible(trialEvaluations.values)
            var hasEligibleCohort = false
            val cohortMatches: MutableList<CohortMatch?>? = Lists.newArrayList()
            for (cohort in trial.cohorts()) {
                if (cohort.metadata().evaluable()) {
                    val cohortEvaluations = evaluateEligibility(patient, cohort.eligibility())
                    val isPotentiallyEligible = isPotentiallyEligible(cohortEvaluations.values)
                    if (isPotentiallyEligible) {
                        hasEligibleCohort = true
                    }
                    cohortMatches.add(
                        ImmutableCohortMatch.builder()
                            .metadata(cohort.metadata())
                            .isPotentiallyEligible(isPotentiallyEligible && passesAllTrialEvaluations)
                            .evaluations(cohortEvaluations)
                            .build()
                    )
                }
            }
            cohortMatches.sort(CohortMatchComparator())
            val isEligible = passesAllTrialEvaluations && (trial.cohorts().isEmpty() || hasEligibleCohort)
            trialMatches.add(
                ImmutableTrialMatch.builder()
                    .identification(trial.identification())
                    .isPotentiallyEligible(isEligible)
                    .evaluations(trialEvaluations)
                    .cohorts(cohortMatches)
                    .build()
            )
        }
        trialMatches.sort(TrialMatchComparator())
        return trialMatches
    }

    private fun evaluateEligibility(patient: PatientRecord, eligibility: MutableList<Eligibility?>): MutableMap<Eligibility?, Evaluation?> {
        val evaluations: MutableMap<Eligibility?, Evaluation?>? = Maps.newTreeMap(EligibilityComparator())
        for (entry in eligibility) {
            val evaluator = evaluationFunctionFactory.create(entry.function())
            evaluations[entry] = evaluator.evaluate(patient)
        }
        return evaluations
    }

    companion object {
        fun create(doidModel: DoidModel, referenceDateProvider: ReferenceDateProvider): TrialMatcher {
            return TrialMatcher(EvaluationFunctionFactory.create(doidModel, referenceDateProvider))
        }

        @VisibleForTesting
        fun isPotentiallyEligible(evaluations: Iterable<Evaluation?>): Boolean {
            for (evaluation in evaluations) {
                if (!evaluation.recoverable() && (evaluation.result() == EvaluationResult.FAIL
                            || evaluation.result() == EvaluationResult.NOT_IMPLEMENTED)
                ) {
                    return false
                }
            }
            return true
        }
    }
}