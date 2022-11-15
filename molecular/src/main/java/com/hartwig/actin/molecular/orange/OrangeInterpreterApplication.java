package com.hartwig.actin.molecular.orange;

import java.io.IOException;
import java.util.List;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMappingTsv;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.filter.GeneFilterFactory;
import com.hartwig.actin.molecular.orange.interpretation.OrangeReader;
import com.hartwig.actin.molecular.orange.serialization.OrangeJson;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ActionableEventsLoader;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.KnownEventsLoader;
import com.hartwig.serve.datamodel.refgenome.RefGenomeVersion;

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

        LOGGER.info("Reading gene filter tsv from {}", config.geneFilterTsv());
        GeneFilter geneFilter = GeneFilterFactory.createFromTsv(config.geneFilterTsv());
        LOGGER.info(" Read {} genes to include from ORANGE record", geneFilter.size());

        LOGGER.info("Reading ORANGE record");
        MolecularRecord molecular = new OrangeReader(geneFilter).read(orange);

        LOGGER.info("Loading ACTIN to external trial mapping TSV from {}", config.externalTrialMappingTsv());
        List<ExternalTrialMapping> mappings = ExternalTrialMappingTsv.read(config.externalTrialMappingTsv());
        LOGGER.info(" Loaded {} mappings", mappings.size());

        KnownEvents knownEvents = KnownEventsLoader.readFromDir(config.serveDirectory(), RefGenomeVersion.V37);
        ActionableEvents actionableEvents = ActionableEventsLoader.readFromDir(config.serveDirectory(), RefGenomeVersion.V37);

        LOGGER.info("Loading clinical json from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());

        // TODO Annotate with clinical interpretation

        MolecularPrinter.printRecord(molecular);
        MolecularRecordJson.write(molecular, config.outputDirectory());

        LOGGER.info("Done!");
    }
}
