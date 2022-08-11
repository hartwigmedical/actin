package com.hartwig.actin.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay;
import com.hartwig.actin.util.Paths;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabularTreatmentMatchWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TabularTreatmentMatchWriterApplication.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DELIMITER = "\t";

    private static final String APPLICATION = "ACTIN Tabular Treatment Match Writer";
    public static final String VERSION = TabularTreatmentMatchWriterApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = TabularTreatmentMatchWriterConfig.createOptions();

        TabularTreatmentMatchWriterConfig config = null;
        try {
            config = TabularTreatmentMatchWriterConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new TabularTreatmentMatchWriterApplication(config).run();
    }

    @NotNull
    private final TabularTreatmentMatchWriterConfig config;

    private TabularTreatmentMatchWriterApplication(@NotNull final TabularTreatmentMatchWriterConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading treatment match results from {}", config.treatmentMatchJson());
        TreatmentMatch treatmentMatch = TreatmentMatchJson.read(config.treatmentMatchJson());

        LOGGER.info("Writing tabular evaluation results to {}", config.outputDirectory());
        String outputPath = Paths.forceTrailingFileSeparator(config.outputDirectory());

        String evaluationSummaryTsv = outputPath + treatmentMatch.sampleId() + ".evaluation.summary.tsv";
        writeEvaluationSummaryToTsv(treatmentMatch, evaluationSummaryTsv);
        LOGGER.info(" Written summary data to {}", evaluationSummaryTsv);

        String evaluationDetailsTsv = outputPath + treatmentMatch.sampleId() + ".evaluation.details.tsv";
        writeEvaluationDetailsToTsv(treatmentMatch, evaluationDetailsTsv);
        LOGGER.info(" Written detailed data to {}", evaluationDetailsTsv);

        LOGGER.info("Done!");
    }

    private static void writeEvaluationSummaryToTsv(@NotNull TreatmentMatch treatmentMatch, @NotNull String tsv) throws IOException {
        List<String> lines = Lists.newArrayList();

        lines.add(createEvaluationSummaryHeader());

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            Set<String> trialFails = extractUnrecoverableFails(trialMatch.evaluations());
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                Set<String> cohortFails = extractUnrecoverableFails(cohortMatch.evaluations());

                StringJoiner record = trialJoiner(trialMatch.identification());
                record.add(cohortMatch.metadata().cohortId());
                record.add(cohortMatch.metadata().description());
                record.add(String.valueOf(cohortMatch.isPotentiallyEligible()));
                record.add("Yes");
                record.add(cohortMatch.isPotentiallyEligible() ? Strings.EMPTY : concat(Sets.union(trialFails, cohortFails)));
                record.add(Strings.EMPTY);
                lines.add(record.toString());
            }

            if (trialMatch.cohorts().isEmpty()) {
                StringJoiner record = trialJoiner(trialMatch.identification());
                record.add(Strings.EMPTY);
                record.add(Strings.EMPTY);
                record.add(String.valueOf(trialMatch.isPotentiallyEligible()));
                record.add("Yes");
                record.add(trialMatch.isPotentiallyEligible() ? Strings.EMPTY : concat(trialFails));
                record.add(Strings.EMPTY);
                lines.add(record.toString());
            }
        }

        Files.write(new File(tsv).toPath(), lines);
    }

    @NotNull
    private static String createEvaluationSummaryHeader() {
        StringJoiner header = tabular();
        header.add("Trial ID");
        header.add("Trial acronym");
        header.add("Cohort ID");
        header.add("Cohort description");
        header.add("Is algorithmically potentially eligible?");
        header.add("Is correct?");
        header.add("Fail messages");
        header.add("Comment");
        return header.toString();
    }

    @NotNull
    private static Set<String> extractUnrecoverableFails(@NotNull Map<Eligibility, Evaluation> evaluations) {
        Set<String> messages = Sets.newTreeSet(Ordering.natural());
        for (Evaluation evaluation : evaluations.values()) {
            if (evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()) {
                messages.addAll(evaluation.failGeneralMessages());
            }
        }
        return messages;
    }

    private static void writeEvaluationDetailsToTsv(@NotNull TreatmentMatch treatmentMatch, @NotNull String tsv) throws IOException {
        List<String> lines = Lists.newArrayList();

        lines.add(createEvaluationDetailsHeader());

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            for (Map.Entry<Eligibility, Evaluation> entry : trialMatch.evaluations().entrySet()) {
                lines.add(toTabularLine(treatmentMatch, trialMatch, null, entry.getKey(), entry.getValue()));
            }

            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                if (cohortMatch.evaluations().isEmpty()) {
                    lines.add(toTabularLine(treatmentMatch, trialMatch, cohortMatch, null, null));
                }
                for (Map.Entry<Eligibility, Evaluation> entry : cohortMatch.evaluations().entrySet()) {
                    lines.add(toTabularLine(treatmentMatch, trialMatch, cohortMatch, entry.getKey(), entry.getValue()));
                }
            }
        }

        Files.write(new File(tsv).toPath(), lines);
    }

    @NotNull
    private static String toTabularLine(@NotNull TreatmentMatch treatmentMatch, @NotNull TrialMatch trialMatch,
            @Nullable CohortMatch cohortMatch, @Nullable Eligibility eligibility, @Nullable Evaluation evaluation) {
        StringJoiner line = tabular();
        line.add(DATE_FORMAT.format(treatmentMatch.referenceDate()));
        line.add(String.valueOf(treatmentMatch.referenceDateIsLive()));
        line.add(trialMatch.identification().trialId());
        line.add(trialMatch.identification().acronym());
        line.add(String.valueOf(trialMatch.identification().open()));
        line.add(String.valueOf(!trialMatch.cohorts().isEmpty()));
        line.add(String.valueOf(trialMatch.isPotentiallyEligible()));
        line.add(cohortMatch != null ? cohortMatch.metadata().cohortId() : Strings.EMPTY);
        line.add(cohortMatch != null ? cohortMatch.metadata().description() : Strings.EMPTY);
        line.add(cohortMatch != null ? String.valueOf(cohortMatch.metadata().open()) : Strings.EMPTY);
        line.add(cohortMatch != null ? String.valueOf(cohortMatch.metadata().slotsAvailable()) : Strings.EMPTY);
        line.add(cohortMatch != null ? String.valueOf(cohortMatch.metadata().blacklist()) : Strings.EMPTY);
        line.add(cohortMatch != null ? String.valueOf(cohortMatch.isPotentiallyEligible()) : Strings.EMPTY);
        line.add(eligibility != null ? EligibilityFunctionDisplay.format(eligibility.function()) : Strings.EMPTY);
        line.add(evaluation != null ? evaluation.result().toString() : Strings.EMPTY);
        line.add(evaluation != null ? String.valueOf(evaluation.recoverable()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.passSpecificMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.passGeneralMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.warnSpecificMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.warnGeneralMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.undeterminedSpecificMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.undeterminedGeneralMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.failSpecificMessages()) : Strings.EMPTY);
        line.add(evaluation != null ? concat(evaluation.failGeneralMessages()) : Strings.EMPTY);
        return line.toString();
    }

    @NotNull
    private static String createEvaluationDetailsHeader() {
        StringJoiner header = tabular();
        header.add("Reference date");
        header.add("Reference date is live?");
        header.add("Trial ID");
        header.add("Trial acronym");
        header.add("Trial is open?");
        header.add("Trial has cohorts?");
        header.add("Is eligible trial?");
        header.add("Cohort ID");
        header.add("Cohort description");
        header.add("Cohort open?");
        header.add("Cohort slots available?");
        header.add("Cohort blacklist?");
        header.add("Is eligible cohort?");
        header.add("Eligibility rule");
        header.add("Eligibility result");
        header.add("Recoverable?");
        header.add("PASS specific messages");
        header.add("PASS general messages");
        header.add("WARN specific messages");
        header.add("WARN general messages");
        header.add("UNDETERMINED specific messages");
        header.add("UNDETERMINED general messages");
        header.add("FAIL specific messages");
        header.add("FAIL general messages");
        return header.toString();
    }

    @NotNull
    private static StringJoiner trialJoiner(@NotNull TrialIdentification identification) {
        StringJoiner joiner = new StringJoiner(DELIMITER);
        joiner.add(identification.trialId());
        joiner.add(identification.acronym());
        return joiner;
    }

    @NotNull
    private static StringJoiner tabular() {
        return new StringJoiner("\t");
    }

    @NotNull
    private static String concat(@NotNull Iterable<String> strings) {
        StringJoiner merged = new StringJoiner(";");
        for (String string : strings) {
            merged.add(string);
        }
        return merged.toString();
    }
}
