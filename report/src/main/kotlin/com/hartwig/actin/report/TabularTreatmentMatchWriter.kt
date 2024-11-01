package com.hartwig.actin.report

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.trial.util.EligibilityFunctionDisplay
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter

object TabularTreatmentMatchWriter {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private const val DELIMITER = "\t"

    @Throws(IOException::class)
    fun writeEvaluationSummaryToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
        File(tsv).bufferedWriter().use { out ->
            writeLine(out, createEvaluationSummaryHeader())
            treatmentMatch.trialMatches.forEach { trialMatch ->
                val trialFails = extractUnrecoverableFails(trialMatch.evaluations)
                trialMatch.cohorts.forEach { cohortMatch ->
                    val cohortFails = extractUnrecoverableFails(cohortMatch.evaluations)
                    val cohortLine = listOf(
                        treatmentMatch.patientId,
                        treatmentMatch.sampleId,
                        trialMatch.identification.trialId,
                        trialMatch.identification.acronym,
                        cohortMatch.metadata.cohortId,
                        cohortMatch.metadata.description,
                        cohortMatch.isPotentiallyEligible.toString(),
                        "Yes",
                        if (cohortMatch.isPotentiallyEligible) "" else concat(trialFails.union(cohortFails).sorted()),
                        ""
                    ).joinToString(DELIMITER)
                    writeLine(out, cohortLine)
                }
                if (trialMatch.cohorts.isEmpty()) {
                    val trialLine = listOf(
                        treatmentMatch.patientId,
                        treatmentMatch.sampleId,
                        trialMatch.identification.trialId,
                        trialMatch.identification.acronym,
                        "",
                        "",
                        trialMatch.isPotentiallyEligible.toString(),
                        "Yes",
                        if (trialMatch.isPotentiallyEligible) "" else concat(trialFails.sorted()),
                        "",
                    ).joinToString(DELIMITER)
                    writeLine(out, trialLine)
                }
            }
        }
    }

    private fun createEvaluationSummaryHeader(): String {
        return concatWithTabs(
            listOf(
                "Patient",
                "Sample ID",
                "Trial ID",
                "Trial acronym",
                "Cohort ID",
                "Cohort description",
                "Is algorithmically potentially eligible?",
                "Is correct?",
                "Fail messages",
                "Comment"
            )
        )
    }

    private fun extractUnrecoverableFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
        return evaluations.values.filter { it.result == EvaluationResult.FAIL && !it.recoverable }
            .flatMap(Evaluation::failGeneralMessages)
            .toSet()
    }

    @Throws(IOException::class)
    fun writeEvaluationDetailsToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
        File(tsv).bufferedWriter().use { out ->
            writeLine(out, createEvaluationDetailsHeader())
            for (trialMatch in treatmentMatch.trialMatches) {
                for ((key, value) in trialMatch.evaluations) {
                    writeLine(out, toTabularLine(treatmentMatch, trialMatch, null, key, value))
                }
                for (cohortMatch in trialMatch.cohorts) {
                    if (cohortMatch.evaluations.isEmpty()) {
                        writeLine(out, toTabularLine(treatmentMatch, trialMatch, cohortMatch, null, null))
                    }
                    for ((key, value) in cohortMatch.evaluations) {
                        writeLine(out, toTabularLine(treatmentMatch, trialMatch, cohortMatch, key, value))
                    }
                }
            }
        }
    }

    private fun toTabularLine(
        treatmentMatch: TreatmentMatch, trialMatch: TrialMatch,
        cohortMatch: CohortMatch?, eligibility: Eligibility?, evaluation: Evaluation?
    ): String {
        val lines = listOf(
            DATE_FORMAT.format(treatmentMatch.referenceDate),
            treatmentMatch.referenceDateIsLive.toString(),
            trialMatch.identification.trialId,
            trialMatch.identification.acronym,
            trialMatch.identification.open.toString(),
            trialMatch.cohorts.isNotEmpty().toString(),
            trialMatch.isPotentiallyEligible.toString(),
            cohortMatch?.metadata?.cohortId ?: "",
            cohortMatch?.metadata?.description ?: "",
            cohortMatch?.metadata?.open?.toString() ?: "",
            cohortMatch?.metadata?.slotsAvailable?.toString() ?: "",
            cohortMatch?.metadata?.ignore?.toString() ?: "",
            cohortMatch?.isPotentiallyEligible?.toString() ?: "",
            if (eligibility != null) EligibilityFunctionDisplay.format(eligibility.function) else "",
            evaluation?.result?.toString() ?: "",
            evaluation?.recoverable?.toString() ?: ""
        ) + evaluationMessageColumns(evaluation)
        return concatWithTabs(lines)
    }

    private fun evaluationMessageColumns(evaluation: Evaluation?): List<String> {
        return if (evaluation == null) {
            List(8) { "" }
        } else {
            listOf(
                evaluation.passSpecificMessages,
                evaluation.passGeneralMessages,
                evaluation.warnSpecificMessages,
                evaluation.warnGeneralMessages,
                evaluation.undeterminedSpecificMessages,
                evaluation.undeterminedGeneralMessages,
                evaluation.failSpecificMessages,
                evaluation.failGeneralMessages

            ).map(::concat)
        }
    }

    private fun createEvaluationDetailsHeader(): String {
        return concatWithTabs(
            listOf(
                "Reference date",
                "Reference date is live?",
                "Trial ID",
                "Trial acronym",
                "Trial is open?",
                "Trial has cohorts?",
                "Is eligible trial?",
                "Cohort ID",
                "Cohort description",
                "Cohort open?",
                "Cohort slots available?",
                "Cohort ignored?",
                "Is eligible cohort?",
                "Eligibility rule",
                "Eligibility result",
                "Recoverable?",
                "PASS specific messages",
                "PASS general messages",
                "WARN specific messages",
                "WARN general messages",
                "UNDETERMINED specific messages",
                "UNDETERMINED general messages",
                "FAIL specific messages",
                "FAIL general messages"
            )
        )
    }

    private fun writeLine(file: BufferedWriter, line: String) {
        file.write(line)
        file.newLine()
    }

    private fun concat(strings: Iterable<String>): String {
        return concat(strings, ";")
    }

    private fun concatWithTabs(strings: Iterable<String>): String {
        return concat(strings, "\t")
    }

    private fun concat(strings: Iterable<String>, separator: String): String {
        return strings.joinToString(separator)
    }
}