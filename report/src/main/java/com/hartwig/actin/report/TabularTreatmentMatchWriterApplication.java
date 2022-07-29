package com.hartwig.actin.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.StringJoiner;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.algo.serialization.TreatmentMatchJson;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class TabularTreatmentMatchWriterApplication {

    private static final Logger LOGGER = LogManager.getLogger(TabularTreatmentMatchWriterApplication.class);

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

        LOGGER.info("Writing tabular treatment matches to {}", config.outputTsv());
        writeTreatmentMatchToTsv(treatmentMatch, config.outputTsv());

        LOGGER.info("Done!");
    }

    private static void writeTreatmentMatchToTsv(@NotNull TreatmentMatch treatmentMatch, @NotNull String tsv) throws IOException {
        List<String> lines = Lists.newArrayList();

        StringJoiner header = tabular();
        header.add("Trial ID");
        header.add("Trial Acronym");
        header.add("Cohort ID");
        header.add("Cohort Description");
        header.add("Is algorithmically potentially eligible?");
        header.add("Is correct?");
        header.add("Comment");
        lines.add(header.toString());

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
}
