package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

object InterpretedCohortFactory {

    fun createEvaluableCohorts(treatmentMatch: TreatmentMatch, filterOnSOCExhaustionAndTumorType: Boolean): List<com.hartwig.actin.report.interpretation.InterpretedCohort> {
        return filteredMatches(
            treatmentMatch.trialMatches, filterOnSOCExhaustionAndTumorType, TrialMatch::evaluations
        ).flatMap { trialMatch: TrialMatch ->
            val trialWarnings = extractWarnings(trialMatch.evaluations)
            val trialFails = extractFails(trialMatch.evaluations)
            val trialInclusionEvents = extractInclusionEvents(trialMatch.evaluations)
            val identification = trialMatch.identification
            val trialId = identification.trialId
            val acronym = identification.acronym
            val trialIsOpen = identification.open
            val phase = identification.phase
            val nctId = identification.nctId
            val isMissingMolecularResultForEvaluation = trialMatch.evaluations.values.any { it.isMissingMolecularResultForEvaluation }
            val source = identification.source
            val sourceId = identification.sourceId
            val locations = identification.locations

            if (trialMatch.cohorts.isEmpty()) {
                listOf(
                    InterpretedCohort(
                        trialId = trialId,
                        acronym = acronym,
                        name = null,
                        molecularEvents = trialInclusionEvents,
                        isPotentiallyEligible = trialMatch.isPotentiallyEligible,
                        isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation,
                        isOpen = trialIsOpen,
                        hasSlotsAvailable = trialIsOpen,
                        warnings = trialWarnings,
                        fails = trialFails,
                        phase = phase,
                        nctId = nctId,
                        source = source,
                        sourceId = sourceId,
                        locations = locations
                    )
                )
            } else {
                filteredMatches(
                    trialMatch.cohorts, filterOnSOCExhaustionAndTumorType, CohortMatch::evaluations
                ).map { cohortMatch: CohortMatch ->
                    com.hartwig.actin.report.interpretation.InterpretedCohort(
                        trialId = trialId,
                        acronym = acronym,
                        name = cohortMatch.metadata.description,
                        molecularEvents = trialInclusionEvents.union(extractInclusionEvents(cohortMatch.evaluations)),
                        isPotentiallyEligible = cohortMatch.isPotentiallyEligible,
                        isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation ||
                                cohortMatch.evaluations.values.any { it.isMissingMolecularResultForEvaluation },
                        isOpen = trialIsOpen && cohortMatch.metadata.open,
                        hasSlotsAvailable = cohortMatch.metadata.slotsAvailable,
                        warnings = trialWarnings.union(extractWarnings(cohortMatch.evaluations)),
                        fails = trialFails.union(extractFails(cohortMatch.evaluations)),
                        phase = phase,
                        nctId = nctId,
                        ignore = cohortMatch.metadata.ignore,
                        source = source,
                        sourceId = sourceId,
                        locations = locations
                    )
                }
            }
        }.sortedWith(InterpretedCohortComparator())
    }

    fun createNonEvaluableCohorts(treatmentMatch: TreatmentMatch): List<com.hartwig.actin.report.interpretation.InterpretedCohort> {
        return treatmentMatch.trialMatches.flatMap { trialMatch: TrialMatch ->
            val identification = trialMatch.identification
            trialMatch.nonEvaluableCohorts.map { cohortMetadata: CohortMetadata ->
                InterpretedCohort(
                    trialId = identification.trialId,
                    acronym = identification.acronym,
                    name = cohortMetadata.description,
                    isOpen = identification.open && cohortMetadata.open,
                    hasSlotsAvailable = cohortMetadata.slotsAvailable,
                    ignore = cohortMetadata.ignore,
                    phase = identification.phase,
                    nctId = identification.nctId,
                    source = identification.source,
                    sourceId = identification.sourceId,
                    locations = identification.locations
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
                    evaluation.failMessages

                evaluation.result == EvaluationResult.WARN ->
                    evaluation.warnMessages

                evaluation.result == EvaluationResult.UNDETERMINED && !evaluation.recoverable ->
                    evaluation.undeterminedMessages

                else -> emptySet()
            }
        }.toSet()
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
            .toSet()
    }
}