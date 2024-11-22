package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.extractDifferences
import com.hartwig.actin.algo.matchcomparison.DifferenceExtractionUtil.mapKeyDifferences
import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.TrialIdentification
import org.apache.logging.log4j.LogManager

object TreatmentMatchComparison {

    private const val INDENT_WIDTH = 2
    private val LOGGER = LogManager.getLogger(TreatmentMatchComparison::class.java)

    fun determineTreatmentMatchDifferences(oldMatches: TreatmentMatch, newMatches: TreatmentMatch): EvaluationDifferences {
        val oldTrialSummary = trialMatchesById(oldMatches)
        val newTrialSummary = trialMatchesById(newMatches)

        val trialKeyDifferences = mapKeyDifferences(oldTrialSummary, newTrialSummary, "trials", TrialIdentification::trialId)

        return oldTrialSummary.map { (key, oldTrialMatch) ->
            val newTrialMatch = newTrialSummary[key]
            if (newTrialMatch == null) EvaluationDifferences.create() else {
                val eligibilityDifferences = extractDifferences(
                    oldTrialMatch, newTrialMatch, mapOf("eligibility" to TrialMatch::isPotentiallyEligible)
                )
                eligibilityDifferences.forEach(::logDebug)

                val evaluationDifferences = EvaluationComparison.determineEvaluationDifferences(
                    oldTrialMatch.evaluations, newTrialMatch.evaluations, key.trialId, 0
                ).copy(eligibilityDifferences = eligibilityDifferences)

                evaluationDifferences + determineCohortDifferences(oldTrialMatch, newTrialMatch)
            }
        }.fold(EvaluationDifferences.create(mapKeyDifferences = trialKeyDifferences)) { acc, other -> acc + other }
    }

    private fun determineCohortDifferences(oldTrialMatch: TrialMatch, newTrialMatch: TrialMatch): EvaluationDifferences {
        val oldCohortSummary = cohortMatchesById(oldTrialMatch)
        val newCohortSummary = cohortMatchesById(newTrialMatch)
        val cohortKeyDifferences = mapKeyDifferences(oldCohortSummary, newCohortSummary, "cohorts") { it }

        return oldCohortSummary.map { (cohortId, oldCohortMatch) ->
            val newCohortMatch = newCohortSummary[cohortId]
            if (newCohortMatch == null) EvaluationDifferences.create() else {
                val cohortEligibilityDifferences = extractDifferences(oldCohortMatch, newCohortMatch, mapOf("eligibility" to CohortMatch::isPotentiallyEligible))
                cohortEligibilityDifferences.forEach { logDebug(it, INDENT_WIDTH) }
                val id = "${oldTrialMatch.identification.trialId}, cohort$cohortId"
                EvaluationComparison.determineEvaluationDifferences(
                    oldCohortMatch.evaluations,
                    newCohortMatch.evaluations,
                    id,
                    INDENT_WIDTH
                )
                    .copy(eligibilityDifferences = cohortEligibilityDifferences)
            }
        }.fold(EvaluationDifferences.create(mapKeyDifferences = cohortKeyDifferences)) { acc, other -> acc + other }
    }

    private fun trialMatchesById(treatmentMatch: TreatmentMatch): Map<TrialIdentification, TrialMatch> {
        return treatmentMatch.trialMatches.associateBy(TrialMatch::identification)
    }

    private fun cohortMatchesById(trialMatch: TrialMatch): Map<String, CohortMatch> {
        return trialMatch.cohorts.associateBy { it.metadata.cohortId }
    }

    private fun logDebug(message: String, indent: Int = 0) {
        LOGGER.debug(" ".repeat(indent) + message)
    }
}