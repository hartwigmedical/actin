package com.hartwig.actin.algo.util

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.StandardOfCareMatch
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer
import com.hartwig.actin.algo.interpretation.TrialMatchSummarizer
import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.TrialIdentification
import com.hartwig.actin.util.DatamodelPrinter

class TreatmentMatchPrinter(private val printer: DatamodelPrinter) {
    
    fun print(treatmentMatch: TreatmentMatch) {
        printer.print("Patient: " + treatmentMatch.patientId)

        val matchSummary = TrialMatchSummarizer.summarize(treatmentMatch.trialMatches)
        printer.print("Trials: " + matchSummary.trialCount)
        printer.print("Eligible trials: " + trialString(matchSummary.eligibleTrialMap))
        printer.print("Cohorts: " + matchSummary.cohortCount)
        printer.print("Eligible cohorts: " + cohortString(matchSummary.eligibleTrialMap))
        printer.print("Eligible and recruiting cohorts: " + recruitingCohortString(matchSummary.eligibleTrialMap))

        val allTrialEvaluations = treatmentMatch.trialMatches.flatMap { trialMatch ->
            trialMatch.cohorts.map(CohortMatch::evaluations) + trialMatch.evaluations
        }
            .flatMap(Map<Eligibility, Evaluation>::values)

        printEvaluationSummary(allTrialEvaluations, "Rules")

        printer.print("Standard-of-care treatments evaluated: ${treatmentMatch.standardOfCareMatches?.count() ?: 0}")
        if (treatmentMatch.standardOfCareMatches != null) {
            printer.print("Eligible SOC treatments: ${treatmentMatch.standardOfCareMatches.count(StandardOfCareMatch::eligible)}")
            printEvaluationSummary(treatmentMatch.standardOfCareMatches.flatMap(StandardOfCareMatch::evaluations), "SOC rules")
        }
    }

    private fun printEvaluationSummary(allTrialEvaluations: List<Evaluation>, typeOfRules: String) {
        val evaluationSummary = EvaluationSummarizer.summarize(allTrialEvaluations)

        printer.print("# $typeOfRules evaluated: " + evaluationSummary.count)
        printer.print("# $typeOfRules with PASS evaluation: " + evaluationSummary.passedCount)
        printer.print("# $typeOfRules with WARN evaluation: " + evaluationSummary.warningCount)
        printer.print("# $typeOfRules with FAIL evaluation: " + evaluationSummary.failedCount)
        printer.print("# $typeOfRules with UNDETERMINED evaluation: " + evaluationSummary.undeterminedCount)
        printer.print("# $typeOfRules which have not been evaluated: " + evaluationSummary.notEvaluatedCount)
        printer.print("# $typeOfRules which have not been implemented: " + evaluationSummary.nonImplementedCount)
    }

    private fun cohortString(eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>>): String {
        val allCohorts = eligibleTrialMap.flatMap { (trial, cohorts) ->
            cohorts.map { cohortName(trial, it) }
        }
        return if (allCohorts.isNotEmpty()) "${allCohorts.size} (${allCohorts.joinToString(", ")})" else "None"
    }

    companion object {
        fun printMatch(treatmentMatch: TreatmentMatch) {
            TreatmentMatchPrinter(DatamodelPrinter.withDefaultIndentation()).print(treatmentMatch)
        }

        private fun trialString(eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>>): String {
            return if (eligibleTrialMap.isEmpty()) "None" else {
                val names = eligibleTrialMap.keys.map(::trialName)
                "${names.size} (${names.joinToString(", ")})"
            }
        }

        private fun trialName(trial: TrialIdentification): String {
            return trial.trialId + " (" + trial.acronym + ")"
        }

        private fun recruitingCohortString(eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>>): String {
            val recruitingCohorts = eligibleTrialMap.flatMap { (trial, cohorts) ->
                cohorts.filter { it.open && it.slotsAvailable }.map { cohortName(trial, it) }
            }
            return if (recruitingCohorts.isNotEmpty()) "${recruitingCohorts.size} (${recruitingCohorts.joinToString(", ")})" else "None"
        }

        private fun cohortName(trial: TrialIdentification, cohort: CohortMetadata): String {
            return trial.trialId + " - " + cohort.description
        }
    }
}
