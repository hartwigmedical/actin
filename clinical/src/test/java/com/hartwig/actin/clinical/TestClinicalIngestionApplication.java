package com.hartwig.actin.clinical;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.curation.CurationModel;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.feed.FeedModel;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.doid.DoidModelFactory;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.serialization.DoidJson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class TestClinicalIngestionApplication {

    private static final Logger LOGGER = LogManager.getLogger(TestClinicalIngestionApplication.class);

    private static final String APPLICATION = "ACTIN Clinical Ingestion";
    private static final String VERSION = TestClinicalIngestionApplication.class.getPackage().getImplementationVersion();
    private static final String FEED_DIRECTORY_PATH =
            String.join(File.separator, List.of(System.getProperty("user.home"), "hmf", "tmp", "feed"));
    private static final String CURATION_DIRECTORY_PATH = String.join(File.separator,
            List.of(System.getProperty("user.home"), "hmf", "repos", "crunch-resources-private", "actin", "clinical_curation"));
    private static final String DOID_JSON_PATH = String.join(File.separator,
            List.of(System.getProperty("user.home"), "hmf", "repos", "common-resources-public", "disease_ontology", "doid.json"));
    private static final String OUTPUT_DIRECTORY_PATH = String.join(File.separator, List.of(FEED_DIRECTORY_PATH, "out"));

    public static void main(@NotNull String... args) throws IOException {
        ClinicalIngestionConfig config = ImmutableClinicalIngestionConfig.builder()
                .feedDirectory(FEED_DIRECTORY_PATH)
                .curationDirectory(CURATION_DIRECTORY_PATH)
                .doidJson(DOID_JSON_PATH)
                .outputDirectory(OUTPUT_DIRECTORY_PATH)
                .build();

        new TestClinicalIngestionApplication(config).run();
    }

    @NotNull
    private final ClinicalIngestionConfig config;

    private TestClinicalIngestionApplication(@NotNull final ClinicalIngestionConfig config) {
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
        CurationModel curationModel = CurationModel.create(config.curationDirectory(), DoidModelFactory.createFromDoidEntry(doidEntry));

        List<ClinicalRecord> records = new ClinicalRecordsFactory(feedModel, curationModel).create();

        String outputDirectory = config.outputDirectory();
        LOGGER.info("Writing {} clinical records to {}", records.size(), outputDirectory);
        ClinicalRecordJson.write(records, outputDirectory);

        LOGGER.info("Done!");
    }
}
