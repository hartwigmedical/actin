package com.hartwig.actin.report

import com.google.common.collect.Lists
import com.google.common.collect.Ordering
import com.google.common.collect.Sets
import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay
import com.hartwig.actin.util.Paths
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.util.Strings
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.format.DateTimeFormatter
import java.util.*

class TabularTreatmentMatchWriterApplication private constructor(private val config: TabularTreatmentMatchWriterConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson())
        val treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson())
        LOGGER.info("Writing tabular evaluation results to {}", config.outputDirectory())
        val outputPath = Paths.forceTrailingFileSeparator(config.outputDirectory())
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
        val VERSION = TabularTreatmentMatchWriterApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = TabularTreatmentMatchWriterConfig.Companion.createOptions()
            var config: TabularTreatmentMatchWriterConfig? = null
            try {
                config = TabularTreatmentMatchWriterConfig.Companion.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            TabularTreatmentMatchWriterApplication(config!!).run()
        }

        @Throws(IOException::class)
        private fun writeEvaluationSummaryToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
            val lines: MutableList<String> = Lists.newArrayList()
            lines.add(createEvaluationSummaryHeader())
            for (trialMatch in treatmentMatch.trialMatches()) {
                val trialFails = extractUnrecoverableFails(trialMatch.evaluations())
                for (cohortMatch in trialMatch.cohorts()) {
                    val cohortFails = extractUnrecoverableFails(cohortMatch.evaluations())
                    val record = trialJoiner(treatmentMatch.patientId(), treatmentMatch.sampleId(), trialMatch.identification())
                    record.add(cohortMatch.metadata().cohortId())
                    record.add(cohortMatch.metadata().description())
                    record.add(cohortMatch.isPotentiallyEligible.toString())
                    record.add("Yes")
                    record.add(if (cohortMatch.isPotentiallyEligible) Strings.EMPTY else concat(Sets.union(trialFails, cohortFails)))
                    record.add(Strings.EMPTY)
                    lines.add(record.toString())
                }
                if (trialMatch.cohorts().isEmpty()) {
                    val record = trialJoiner(treatmentMatch.patientId(), treatmentMatch.sampleId(), trialMatch.identification())
                    record.add(Strings.EMPTY)
                    record.add(Strings.EMPTY)
                    record.add(trialMatch.isPotentiallyEligible.toString())
                    record.add("Yes")
                    record.add(if (trialMatch.isPotentiallyEligible) Strings.EMPTY else concat(trialFails))
                    record.add(Strings.EMPTY)
                    lines.add(record.toString())
                }
            }
            Files.write(File(tsv).toPath(), lines)
        }

        private fun createEvaluationSummaryHeader(): String {
            val header = tabular()
            header.add("Patient")
            header.add("Sample ID")
            header.add("Trial ID")
            header.add("Trial acronym")
            header.add("Cohort ID")
            header.add("Cohort description")
            header.add("Is algorithmically potentially eligible?")
            header.add("Is correct?")
            header.add("Fail messages")
            header.add("Comment")
            return header.toString()
        }

        private fun extractUnrecoverableFails(evaluations: Map<Eligibility, Evaluation>): Set<String> {
            val messages: MutableSet<String> = Sets.newTreeSet(Ordering.natural())
            for (evaluation in evaluations.values) {
                if (evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()) {
                    messages.addAll(evaluation.failGeneralMessages())
                }
            }
            return messages
        }

        private fun trialJoiner(patientId: String, sampleId: String, identification: TrialIdentification): StringJoiner {
            val joiner = StringJoiner(DELIMITER)
            joiner.add(patientId)
            joiner.add(sampleId)
            joiner.add(identification.trialId())
            joiner.add(identification.acronym())
            return joiner
        }

        @Throws(IOException::class)
        private fun writeEvaluationDetailsToTsv(treatmentMatch: TreatmentMatch, tsv: String) {
            val lines: MutableList<String> = Lists.newArrayList()
            lines.add(createEvaluationDetailsHeader())
            for (trialMatch in treatmentMatch.trialMatches()) {
                for ((key, value) in trialMatch.evaluations()) {
                    lines.add(toTabularLine(treatmentMatch, trialMatch, null, key, value))
                }
                for (cohortMatch in trialMatch.cohorts()) {
                    if (cohortMatch.evaluations().isEmpty()) {
                        lines.add(toTabularLine(treatmentMatch, trialMatch, cohortMatch, null, null))
                    }
                    for ((key, value) in cohortMatch.evaluations()) {
                        lines.add(toTabularLine(treatmentMatch, trialMatch, cohortMatch, key, value))
                    }
                }
            }
            Files.write(File(tsv).toPath(), lines)
        }

        private fun toTabularLine(
            treatmentMatch: TreatmentMatch, trialMatch: TrialMatch,
            cohortMatch: CohortMatch?, eligibility: Eligibility?, evaluation: Evaluation?
        ): String {
            val line = tabular()
            line.add(DATE_FORMAT.format(treatmentMatch.referenceDate()))
            line.add(treatmentMatch.referenceDateIsLive().toString())
            line.add(trialMatch.identification().trialId())
            line.add(trialMatch.identification().acronym())
            line.add(trialMatch.identification().open().toString())
            line.add(!trialMatch.cohorts().isEmpty().toString())
            line.add(trialMatch.isPotentiallyEligible.toString())
            line.add(cohortMatch?.metadata()?.cohortId() ?: Strings.EMPTY)
            line.add(cohortMatch?.metadata()?.description() ?: Strings.EMPTY)
            line.add(cohortMatch?.metadata()?.open()?.toString() ?: Strings.EMPTY)
            line.add(cohortMatch?.metadata()?.slotsAvailable()?.toString() ?: Strings.EMPTY)
            line.add(cohortMatch?.metadata()?.blacklist()?.toString() ?: Strings.EMPTY)
            line.add(cohortMatch?.isPotentiallyEligible?.toString() ?: Strings.EMPTY)
            line.add(if (eligibility != null) EligibilityFunctionDisplay.format(eligibility.function()) else Strings.EMPTY)
            line.add(evaluation?.result()?.toString() ?: Strings.EMPTY)
            line.add(evaluation?.recoverable()?.toString() ?: Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.passSpecificMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.passGeneralMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.warnSpecificMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.warnGeneralMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.undeterminedSpecificMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.undeterminedGeneralMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.failSpecificMessages()) else Strings.EMPTY)
            line.add(if (evaluation != null) concat(evaluation.failGeneralMessages()) else Strings.EMPTY)
            return line.toString()
        }

        private fun createEvaluationDetailsHeader(): String {
            val header = tabular()
            header.add("Reference date")
            header.add("Reference date is live?")
            header.add("Trial ID")
            header.add("Trial acronym")
            header.add("Trial is open?")
            header.add("Trial has cohorts?")
            header.add("Is eligible trial?")
            header.add("Cohort ID")
            header.add("Cohort description")
            header.add("Cohort open?")
            header.add("Cohort slots available?")
            header.add("Cohort blacklist?")
            header.add("Is eligible cohort?")
            header.add("Eligibility rule")
            header.add("Eligibility result")
            header.add("Recoverable?")
            header.add("PASS specific messages")
            header.add("PASS general messages")
            header.add("WARN specific messages")
            header.add("WARN general messages")
            header.add("UNDETERMINED specific messages")
            header.add("UNDETERMINED general messages")
            header.add("FAIL specific messages")
            header.add("FAIL general messages")
            return header.toString()
        }

        private fun tabular(): StringJoiner {
            return StringJoiner("\t")
        }

        private fun concat(strings: Iterable<String>): String {
            val merged = StringJoiner(";")
            for (string in strings) {
                merged.add(string)
            }
            return merged.toString()
        }
    }
}