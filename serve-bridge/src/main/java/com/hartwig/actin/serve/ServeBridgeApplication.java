package com.hartwig.actin.serve;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.serialization.ServeRecordTsv;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.serialization.TrialJson;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ServeBridgeApplication {

    private static final Logger LOGGER = LogManager.getLogger(ServeBridgeApplication.class);

    private static final String APPLICATION = "ACTIN SERVE-Bridge";
    public static final String VERSION = ServeBridgeApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = ServeBridgeConfig.createOptions();

        ServeBridgeConfig config = null;
        try {
            config = ServeBridgeConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new ServeBridgeApplication(config).run();
    }

    @NotNull
    private final ServeBridgeConfig config;

    private ServeBridgeApplication(@NotNull final ServeBridgeConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Loading trials from {}", config.treatmentDatabaseDirectory());
        List<Trial> trials = TrialJson.readFromDir(config.treatmentDatabaseDirectory());
        LOGGER.info(" Loaded {} trials", trials.size());

        LOGGER.info("Creating SERVE records");
        Set<ServeRecord> records = ServeRecordExtractor.extract(trials);
        LOGGER.info(" Extracted {} records from {} trials", records.size(), trials.size());

        LOGGER.info("Writing SERVE records to {}", config.outputServeKnowledgebaseTsv());
        ServeRecordTsv.write(config.outputServeKnowledgebaseTsv(), records);

        LOGGER.info("Done!");
    }
}
