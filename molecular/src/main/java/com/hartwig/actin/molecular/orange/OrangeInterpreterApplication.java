package com.hartwig.actin.molecular.orange;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.interpretation.OrangeInterpreter;
import com.hartwig.actin.molecular.orange.serialization.OrangeJson;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.serve.serialization.ServeRecordTsv;

import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class OrangeInterpreterApplication {

    private static final Logger LOGGER = LogManager.getLogger(OrangeInterpreterApplication.class);

    private static final String APPLICATION = "ACTIN ORANGE Interpreter";
    private static final String VERSION = OrangeInterpreterApplication.class.getPackage().getImplementationVersion();

    public static void main(@NotNull String... args) throws IOException {
        Options options = OrangeInterpreterConfig.createOptions();

        OrangeInterpreterConfig config = null;
        try {
            config = OrangeInterpreterConfig.createConfig(new DefaultParser().parse(options, args));
        } catch (ParseException exception) {
            LOGGER.warn(exception);
            new HelpFormatter().printHelp(APPLICATION, options);
            System.exit(1);
        }

        new OrangeInterpreterApplication(config).run();
    }

    @NotNull
    private final OrangeInterpreterConfig config;

    private OrangeInterpreterApplication(@NotNull final OrangeInterpreterConfig config) {
        this.config = config;
    }

    public void run() throws IOException {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION);

        LOGGER.info("Reading ORANGE json from {}", config.orangeJson());
        OrangeRecord orange = OrangeJson.read(config.orangeJson());

        LOGGER.info("Loading SERVE bridge TSV from {}", config.serveBridgeTsv());
        List<ServeRecord> records = ServeRecordTsv.read(config.serveBridgeTsv());
        LOGGER.info(" Loaded {} records", records.size());

        LOGGER.info("Interpreting ORANGE record");
        MolecularRecord molecular = OrangeInterpreter.fromServeRecords(records).interpret(orange);
        MolecularPrinter.printRecord(molecular);

        MolecularRecordJson.write(molecular, config.outputDirectory());

        LOGGER.info("Done!");
    }
}
