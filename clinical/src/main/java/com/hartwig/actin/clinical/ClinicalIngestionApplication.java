package com.hartwig.actin.clinical;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.serialization.DoidJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ClinicalIngestionApplication {

    private static final Logger LOGGER = LogManager.getLogger(ClinicalIngestionApplication.class);

    private static final String APPLICATION = "ACTIN Clinical Ingestion";
    private static final String VERSION = ClinicalIngestionApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = ClinicalIngestionConfig.createOptions();

        ClinicalIngestionConfig config = null;
        try {
            config = ClinicalIngestionConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
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
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading DOID tree from {}", config.doidJson());
        DoidEntry doidEntry = DoidJson.readDoidOwlEntry(config.doidJson());
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size());

        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory());
        FeedModel feedModel = FeedModel.fromFeedDirectory(config.feedDirectory());

        LOGGER.info("Creating clinical curation model from directory {}", config.curationDirectory());
        CurationModel curationModel = CurationModel.fromCurationDirectory(config.curationDirectory());

        List<ClinicalRecord> records = new ClinicalRecordsFactory(feedModel, curationModel).create();

        String outputDirectory = config.outputDirectory();
        LOGGER.info("Writing {} clinical records to {}", records.size(), outputDirectory);
        ClinicalRecordJson.write(records, outputDirectory);

        LOGGER.info("Done!");
    }
}
