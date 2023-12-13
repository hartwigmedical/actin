package com.hartwig.actin.algo.util

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.interpretation.EvaluationSummarizer
import com.hartwig.actin.algo.interpretation.EvaluationSummary
import com.hartwig.actin.algo.interpretation.TrialMatchSummarizer
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.util.DatamodelPrinter
import java.util.*

class TreatmentMatchPrinter private constructor(private val printer: DatamodelPrinter) {
    fun print(treatmentMatch: TreatmentMatch) {
        printer.print("Patient: " + treatmentMatch.patientId())
        val matchSummary = TrialMatchSummarizer.summarize(treatmentMatch.trialMatches())
        printer.print("Trials: " + matchSummary.trialCount())
        printer.print("Eligible trials: " + trialString(matchSummary.eligibleTrialMap()))
        printer.print("Cohorts: " + matchSummary.cohortCount())
        printer.print("Eligible cohorts: " + cohortString(matchSummary.eligibleTrialMap()))
        printer.print("Eligible and recruiting cohorts: " + recruitingCohortString(matchSummary.eligibleTrialMap()))
        val summaries: MutableList<EvaluationSummary> = Lists.newArrayList()
        for (trialMatch in treatmentMatch.trialMatches()) {
            summaries.add(EvaluationSummarizer.summarize(trialMatch!!.evaluations().values))
            for (cohortMatch in trialMatch.cohorts()) {
                summaries.add(EvaluationSummarizer.summarize(cohortMatch!!.evaluations().values))
            }
        }
        val evaluationSummary = EvaluationSummarizer.sum(summaries)
        printer.print("# Rules evaluated: " + evaluationSummary.count())
        printer.print("# Rules with PASS evaluation: " + evaluationSummary.passedCount())
        printer.print("# Rules with WARN evaluation: " + evaluationSummary.warningCount())
        printer.print("# Rules with FAIL evaluation: " + evaluationSummary.failedCount())
        printer.print("# Rules with UNDETERMINED evaluation: " + evaluationSummary.undeterminedCount())
        printer.print("# Rules which have not been evaluated: " + evaluationSummary.notEvaluatedCount())
        printer.print("# Rules which have not been implemented: " + evaluationSummary.nonImplementedCount())
    }

    private fun cohortString(eligibleTrialMap: Map<TrialIdentification?, List<CohortMetadata?>?>): String {
        var cohortCount = 0
        val joiner = StringJoiner(", ")
        for ((key, value) in eligibleTrialMap) {
            for (cohort in value!!) {
                cohortCount++
                joiner.add(cohortName(key, cohort))
            }
        }
        return if (cohortCount > 0) "$cohortCount ($joiner)" else "None"
    }

    companion object {
        @JvmStatic
        fun printMatch(treatmentMatch: TreatmentMatch) {
            TreatmentMatchPrinter(DatamodelPrinter.withDefaultIndentation()).print(treatmentMatch)
        }

        private fun trialString(eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>>): String {
            if (eligibleTrialMap.isEmpty()) {
                return "None"
            }
            val joiner = StringJoiner(", ")
            for (trial in eligibleTrialMap.keys) {
                joiner.add(trialName(trial))
            }
            return eligibleTrialMap.keys.size.toString() + " (" + joiner + ")"
        }

        private fun trialName(trial: TrialIdentification): String {
            return trial.trialId() + " (" + trial.acronym() + ")"
        }

        private fun recruitingCohortString(eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>>): String {
            var recruitingCohortCount = 0
            val joiner = StringJoiner(", ")
            for ((key, value) in eligibleTrialMap) {
                for (cohort in value) {
                    if (cohort.open() && cohort.slotsAvailable()) {
                        recruitingCohortCount++
                        joiner.add(cohortName(key, cohort))
                    }
                }
            }
            return if (recruitingCohortCount > 0) "$recruitingCohortCount ($joiner)" else "None"
        }

        private fun cohortName(trial: TrialIdentification, cohort: CohortMetadata): String {
            return trial.trialId() + " - " + cohort.description()
        }
    }
}
