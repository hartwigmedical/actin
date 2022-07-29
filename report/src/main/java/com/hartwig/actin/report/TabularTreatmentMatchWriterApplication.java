package com.hartwig.actin.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
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
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                StringJoiner record = trialJoiner(trialMatch.identification());
                record.add(cohortMatch.metadata().cohortId());
                record.add(cohortMatch.metadata().description());
                record.add(String.valueOf(cohortMatch.isPotentiallyEligible()));
                record.add(Strings.EMPTY);
                record.add(Strings.EMPTY);
                lines.add(record.toString());
            }

            if (trialMatch.cohorts().isEmpty()) {
                StringJoiner record = trialJoiner(trialMatch.identification());
                record.add(Strings.EMPTY);
                record.add(Strings.EMPTY);
                record.add(String.valueOf(trialMatch.isPotentiallyEligible()));
                record.add(Strings.EMPTY);
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
        header.add("Comment");
        return header.toString();
    }

    private static void writeEvaluationDetailsToTsv(@NotNull TreatmentMatch treatmentMatch, @NotNull String tsv) throws IOException {
        List<String> lines = Lists.newArrayList();

        lines.add(createEvaluationDetailsHeader());

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            for (Map.Entry<Eligibility, Evaluation> entry : trialMatch.evaluations().entrySet()) {
                lines.add(toTabularLine(treatmentMatch, trialMatch, null, entry.getKey(), entry.getValue()));
            }

            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                for (Map.Entry<Eligibility, Evaluation> entry : cohortMatch.evaluations().entrySet()) {
                    lines.add(toTabularLine(treatmentMatch, trialMatch, cohortMatch, entry.getKey(), entry.getValue()));
                }
            }
        }

        Files.write(new File(tsv).toPath(), lines);
    }

    @NotNull
    private static String toTabularLine(@NotNull TreatmentMatch treatmentMatch, @NotNull TrialMatch trialMatch,
            @Nullable CohortMatch cohortMatch, @NotNull Eligibility eligibility, @NotNull Evaluation evaluation) {
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
        line.add(EligibilityFunctionDisplay.format(eligibility.function()));
        line.add(evaluation.result().toString());
        line.add(String.valueOf(evaluation.recoverable()));
        line.add(concat(evaluation.passSpecificMessages()));
        line.add(concat(evaluation.passGeneralMessages()));
        line.add(concat(evaluation.warnSpecificMessages()));
        line.add(concat(evaluation.warnGeneralMessages()));
        line.add(concat(evaluation.undeterminedSpecificMessages()));
        line.add(concat(evaluation.undeterminedGeneralMessages()));
        line.add(concat(evaluation.failSpecificMessages()));
        line.add(concat(evaluation.failGeneralMessages()));
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
