package com.hartwig.actin.report

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay
import com.hartwig.actin.util.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.system.exitProcess

class TabularTreatmentMatchWriterApplication private constructor(private val config: TabularTreatmentMatchWriterConfig) {

    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson)
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson)

        LOGGER.info("Writing tabular evaluation results to {}", config.outputDirectory)
        val outputPath = Paths.forceTrailingFileSeparator(config.outputDirectory)
        val evaluationSummaryTsv = outputPath + treatmentMatch.patientId() + ".evaluation.summary.tsv"
        writeEvaluationSummaryToTsv(treatmentMatch, evaluationSummaryTsv)
        LOGGER.info(" Written summary data to {}", evaluationSummaryTsv)

        val evaluationDetailsTsv = outputPath + treatmentMatch.patientId() + ".evaluation.details.tsv"
        writeEvaluationDetailsToTsv(treatmentMatch, evaluationDetailsTsv)
        LOGGER.info(" Written detailed data to {}", evaluationDetailsTsv)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            TabularTreatmentMatchWriterApplication::class.java
        )
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private const val DELIMITER = "\t"
        private const val APPLICATION = "ACTIN Tabular Treatment Match Writer"
        val VERSION: String = TabularTreatmentMatchWriterApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = TabularTreatmentMatchWriterConfig.createOptions()
            try {
                val config = TabularTreatmentMatchWriterConfig.createConfig(DefaultParser().parse(options, args))
                TabularTreatmentMatchWriterApplication(config).run()
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                exitProcess(1)
            }
        }

        @Throws(IOException::class)
        private fun writeEvaluationSummaryToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
            File(tsv).bufferedWriter().use { out ->
                writeLine(out, createEvaluationSummaryHeader())
                treatmentMatch.trialMatches().forEach { trialMatch ->
                    val trialFails = extractUnrecoverableFails(trialMatch.evaluations())
                    trialMatch.cohorts().forEach { cohortMatch ->
                        val cohortFails = extractUnrecoverableFails(cohortMatch.evaluations())
                        val cohortLine = listOf(
                            treatmentMatch.patientId(),
                            treatmentMatch.sampleId(),
                            trialMatch.identification().trialId(),
                            trialMatch.identification().acronym(),
                            cohortMatch.metadata().cohortId(),
                            cohortMatch.metadata().description(),
                            cohortMatch.isPotentiallyEligible.toString(),
                            "Yes",
                            if (cohortMatch.isPotentiallyEligible) "" else concat(trialFails.union(cohortFails).sorted()),
                            ""
                        ).joinToString(DELIMITER)
                        writeLine(out, cohortLine)
                    }
                    if (trialMatch.cohorts().isEmpty()) {
                        val trialLine = listOf(
                            treatmentMatch.patientId(),
                            treatmentMatch.sampleId(),
                            trialMatch.identification().trialId(),
                            trialMatch.identification().acronym(),
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
            return evaluations.values.filter { it.result() == EvaluationResult.FAIL && !it.recoverable() }
                .flatMap(Evaluation::failGeneralMessages)
                .toSet()
        }

        @Throws(IOException::class)
        private fun writeEvaluationDetailsToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
            File(tsv).bufferedWriter().use { out ->
                writeLine(out, createEvaluationDetailsHeader())
                for (trialMatch in treatmentMatch.trialMatches()) {
                    for ((key, value) in trialMatch.evaluations()) {
                        writeLine(out, toTabularLine(treatmentMatch, trialMatch, null, key, value))
                    }
                    for (cohortMatch in trialMatch.cohorts()) {
                        if (cohortMatch.evaluations().isEmpty()) {
                            writeLine(out, toTabularLine(treatmentMatch, trialMatch, cohortMatch, null, null))
                        }
                        for ((key, value) in cohortMatch.evaluations()) {
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
                DATE_FORMAT.format(treatmentMatch.referenceDate()),
                treatmentMatch.referenceDateIsLive().toString(),
                trialMatch.identification().trialId(),
                trialMatch.identification().acronym(),
                trialMatch.identification().open().toString(),
                trialMatch.cohorts().isNotEmpty().toString(),
                trialMatch.isPotentiallyEligible.toString(),
                cohortMatch?.metadata()?.cohortId() ?: "",
                cohortMatch?.metadata()?.description() ?: "",
                cohortMatch?.metadata()?.open()?.toString() ?: "",
                cohortMatch?.metadata()?.slotsAvailable()?.toString() ?: "",
                cohortMatch?.metadata()?.blacklist()?.toString() ?: "",
                cohortMatch?.isPotentiallyEligible?.toString() ?: "",
                if (eligibility != null) EligibilityFunctionDisplay.format(eligibility.function()) else "",
                evaluation?.result()?.toString() ?: "",
                evaluation?.recoverable()?.toString() ?: ""
            ) + evaluationMessageColumns(evaluation)
            return concatWithTabs(lines)
        }

        private fun evaluationMessageColumns(evaluation: Evaluation?): List<String> {
            return if (evaluation == null) {
                List(8) { "" }
            } else {
                listOf(
                    evaluation.passSpecificMessages(),
                    evaluation.passGeneralMessages(),
                    evaluation.warnSpecificMessages(),
                    evaluation.warnGeneralMessages(),
                    evaluation.undeterminedSpecificMessages(),
                    evaluation.undeterminedGeneralMessages(),
                    evaluation.failSpecificMessages(),
                    evaluation.failGeneralMessages()

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
                    "Cohort blacklist?",
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
}