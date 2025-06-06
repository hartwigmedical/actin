package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

object InterpretedCohortFactory {

    fun createEvaluableCohorts(treatmentMatch: TreatmentMatch, filterOnSOCExhaustionAndTumorType: Boolean): List<InterpretedCohort> {
        return filteredMatches(
            treatmentMatch.trialMatches, filterOnSOCExhaustionAndTumorType, TrialMatch::evaluations
        ).flatMap { trialMatch: TrialMatch ->
            val trialWarnings = extractWarnings(trialMatch.evaluations)
            val trialFails = extractFails(trialMatch.evaluations)
            val trialInclusionEvents = extractInclusionEvents(trialMatch.evaluations)
            val identification = trialMatch.identification
            val isMissingMolecularResultForEvaluation = trialMatch.evaluations.values.any { it.isMissingMolecularResultForEvaluation }

            if (trialMatch.cohorts.isEmpty()) {
                listOf(
                    InterpretedCohort(
                        trialId = identification.trialId,
                        acronym = identification.acronym,
                        nctId = identification.nctId,
                        title = identification.title,
                        phase = identification.phase,
                        source = identification.source,
                        sourceId = identification.sourceId,
                        locations = identification.locations,
                        url = identification.url,
                        name = null,
                        isOpen = identification.open,
                        hasSlotsAvailable = identification.open,
                        ignore = false,
                        isEvaluable = true,
                        molecularEvents = trialInclusionEvents,
                        isPotentiallyEligible = trialMatch.isPotentiallyEligible,
                        isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation,
                        warnings = trialWarnings,
                        fails = trialFails
                    )
                )
            } else {
                filteredMatches(
                    trialMatch.cohorts, filterOnSOCExhaustionAndTumorType, CohortMatch::evaluations
                ).map { cohortMatch: CohortMatch ->
                    InterpretedCohort(
                        trialId = identification.trialId,
                        acronym = identification.acronym,
                        nctId = identification.nctId,
                        title = identification.title,
                        phase = identification.phase,
                        source = identification.source,
                        sourceId = identification.sourceId,
                        locations = identification.locations,
                        url = identification.url,
                        name = cohortMatch.metadata.description,
                        isOpen = identification.open && cohortMatch.metadata.open,
                        hasSlotsAvailable = cohortMatch.metadata.slotsAvailable,
                        ignore = cohortMatch.metadata.ignore,
                        isEvaluable = true,
                        molecularEvents = trialInclusionEvents.union(extractInclusionEvents(cohortMatch.evaluations)),
                        isPotentiallyEligible = cohortMatch.isPotentiallyEligible,
                        isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation ||
                                cohortMatch.evaluations.values.any { it.isMissingMolecularResultForEvaluation },
                        warnings = trialWarnings.union(extractWarnings(cohortMatch.evaluations)),
                        fails = trialFails.union(extractFails(cohortMatch.evaluations))
                    )
                }
            }
        }.sortedWith(InterpretedCohortComparator())
    }

    fun createNonEvaluableCohorts(treatmentMatch: TreatmentMatch): List<InterpretedCohort> {
        return treatmentMatch.trialMatches.flatMap { trialMatch: TrialMatch ->
            val identification = trialMatch.identification
            trialMatch.nonEvaluableCohorts.map { cohortMetadata: CohortMetadata ->
                InterpretedCohort(
                    trialId = identification.trialId,
                    acronym = identification.acronym,
                    nctId = identification.nctId,
                    title = identification.title,
                    phase = identification.phase,
                    source = identification.source,
                    sourceId = identification.sourceId,
                    locations = identification.locations,
                    url = identification.url,
                    name = cohortMetadata.description,
                    isOpen = identification.open && cohortMetadata.open,
                    hasSlotsAvailable = cohortMetadata.slotsAvailable,
                    ignore = cohortMetadata.ignore,
                    isEvaluable = false,
                    molecularEvents = emptySet(),
                    isPotentiallyEligible = false,
                    isMissingMolecularResultForEvaluation = false,
                    warnings = emptySet(),
                    fails = emptySet(),
                )
            }
        }.sortedWith(InterpretedCohortComparator())
    }

    private fun extractInclusionEvents(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        return evaluationMap.values.flatMap(Evaluation::inclusionMolecularEvents).toSet()
    }

    private fun extractWarnings(evaluationMap: Map<Eligibility, Evaluation>): Set<String> {
        return evaluationMap.values.flatMap { evaluation ->
            when {
                evaluation.result == EvaluationResult.FAIL && evaluation.recoverable ->
                    combineMessages(evaluation.failMessages)

                evaluation.result == EvaluationResult.WARN ->
                    combineMessages(evaluation.warnMessages)

                evaluation.result == EvaluationResult.UNDETERMINED && !evaluation.recoverable ->
                    combineMessages(evaluation.undeterminedMessages)

                else -> emptySet()
            }
        }.map { it.toString() }.toSet()
    }

    private fun combineMessages(evaluations: Set<EvaluationMessage>): List<EvaluationMessage> {
        return evaluations.groupBy { it.combineBy() }
            .mapValues { it.value.fold(StaticMessage("") as EvaluationMessage) { i, r -> i.combine(r) } }.values.toList()
    }

    private fun <T> filteredMatches(
        matches: List<T>, filterOnSOCExhaustionAndTumorType: Boolean, evaluations: (T) -> Map<Eligibility, Evaluation>
    ) = if (!filterOnSOCExhaustionAndTumorType) matches else {
        matches.filter {
            val trialWarningsAndFails = extractWarnings(evaluations(it)) + extractFails(evaluations(it))
            !trialWarningsAndFails.any { trialWarningOrFail -> trialWarningOrFail.contains("Has not exhausted SOC") }
                    && "Tumor type" !in trialWarningsAndFails
        }
    }

    private fun extractFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
        return evaluations.values.filter { it.result == EvaluationResult.FAIL && !it.recoverable }
            .flatMap(Evaluation::failMessages)
            .map { it.toString() }
            .toSet()
    }
}