package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;
import com.hartwig.actin.serialization.ClinicalRecordFile;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ClinicalIngestionApplication {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalIngestionApplication.class);

    private static final String VERSION = ClinicalIngestionApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        LOGGER.info("Running ACTIN Clinical Ingestion v{}", VERSION);

        Options options = ClinicalIngestionConfig.createOptions();

        ClinicalIngestionConfig config = null;
        try {
            config = ClinicalIngestionConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp("ACTIN Clinical Ingestion", options);
            System.exit(1);
        }

        new ClinicalIngestionApplication(config).run();
    }

    @NotNull
    private final ClinicalIngestionConfig config;

    private ClinicalIngestionApplication(@NotNull final ClinicalIngestionConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        String feedDirectory = config.feedDirectory();
        String curationDirectory = config.curationDirectory();

        LOGGER.info("Creating clinical model from feed directory {} and curation direction {}", feedDirectory, curationDirectory);
        List<ClinicalRecord> records = ClinicalRecordFactory.fromFeedAndCurationDirectories(feedDirectory, curationDirectory);

        String outputDirectory = config.outputDirectory();
        LOGGER.info("Writing {} clinical records to {}", records.size(), outputDirectory);
        ClinicalRecordFile.write(records, outputDirectory);

        LOGGER.info("Done!");
    }
}
