package com.hartwig.actin.molecular.orange;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.serialization.DoidJson;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.GeneFilterFactory;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabaseFactory;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMappingFile;
import com.hartwig.actin.molecular.orange.interpretation.OrangeInterpreter;
import com.hartwig.actin.molecular.serialization.MolecularRecordJson;
import com.hartwig.actin.molecular.util.MolecularPrinter;
import com.hartwig.hmftools.datamodel.OrangeJson;
import com.hartwig.hmftools.datamodel.cuppa.CuppaData;
import com.hartwig.hmftools.datamodel.cuppa.CuppaPrediction;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ActionableEventsLoader;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.KnownEventsLoader;
import com.hartwig.serve.datamodel.RefGenome;

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
        OrangeRecord orange = OrangeJson.getInstance().read(config.orangeJson());
        validateOrangeRecord(orange);

        LOGGER.info("Loading evidence database");
        RefGenome serveRefGenomeVersion = toServeRefGenomeVersion(orange.refGenomeVersion());
        KnownEvents knownEvents = KnownEventsLoader.readFromDir(config.serveDirectory(), serveRefGenomeVersion);
        EvidenceDatabase evidenceDatabase = loadEvidenceDatabase(config, serveRefGenomeVersion, knownEvents);

        LOGGER.info("Interpreting ORANGE record");
        GeneFilter geneFilter = GeneFilterFactory.createFromKnownGenes(knownEvents.genes());
        MolecularRecord molecular = new OrangeInterpreter(geneFilter, evidenceDatabase).interpret(orange);

        MolecularPrinter.printRecord(molecular);
        MolecularRecordJson.write(molecular, config.outputDirectory());

        LOGGER.info("Done!");
    }

    @NotNull
    private static EvidenceDatabase loadEvidenceDatabase(@NotNull OrangeInterpreterConfig config, @NotNull RefGenome serveRefGenomeVersion,
            @NotNull KnownEvents knownEvents) throws IOException {
        ActionableEvents actionableEvents = ActionableEventsLoader.readFromDir(config.serveDirectory(), serveRefGenomeVersion);

        LOGGER.info("Loading external trial to ACTIN mapping TSV from {}", config.externalTrialMappingTsv());
        List<ExternalTrialMapping> mappings = ExternalTrialMappingFile.read(config.externalTrialMappingTsv());
        LOGGER.info(" Loaded {} mappings", mappings.size());

        LOGGER.info("Loading clinical json from {}", config.clinicalJson());
        ClinicalRecord clinical = ClinicalRecordJson.read(config.clinicalJson());
        Set<String> tumorDoids = clinical.tumor().doids();
        if (tumorDoids == null || tumorDoids.isEmpty()) {
            LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", clinical.patientId());
        } else {
            LOGGER.info(" Tumor DOIDs determined to be: {}", concat(tumorDoids));
        }

        LOGGER.info("Loading DOID tree from {}", config.doidJson());
        DoidEntry doidEntry = DoidJson.readDoidOwlEntry(config.doidJson());
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size());

        return EvidenceDatabaseFactory.create(knownEvents, actionableEvents, mappings, doidEntry, tumorDoids);
    }

    @NotNull
    private static RefGenome toServeRefGenomeVersion(@NotNull OrangeRefGenomeVersion refGenomeVersion) {
        switch (refGenomeVersion) {
            case V37: {
                return RefGenome.V37;
            }
            case V38: {
                return RefGenome.V38;
            }
        }

        throw new IllegalStateException("Could not convert ORANGE ref genome version to SERVE ref genome version: " + refGenomeVersion);
    }

    private static void validateOrangeRecord(OrangeRecord orange) {
        throwIfGermlineFieldNonEmpty(orange);
        throwIfCuppaPredictionClassifierMissing(orange);
    }

    private static void throwIfGermlineFieldNonEmpty(OrangeRecord orange) {
        final String message = "must be null or empty because ACTIN only accepts ORANGE output that has been "
                + "scrubbed of germline data. Please use the JSON output from the 'orange_no_germline' directory.";

        if (!orange.linx().allGermlineStructuralVariants().isEmpty()) {
            throw new RuntimeException("allGermlineStructuralVariants " + message);
        }

        if (!orange.linx().allGermlineBreakends().isEmpty()) {
            throw new RuntimeException("allGermlineStructuralVariants " + message);
        }

        if (!orange.linx().germlineHomozygousDisruptions().isEmpty()) {
            throw new RuntimeException("germlineHomozygousDisruptions " + message);
        }
    }

    private static void throwIfCuppaPredictionClassifierMissing(OrangeRecord orange) {
        final String message = "Missing field %s: cuppa not run in expected configuration";
        CuppaData cuppaData = orange.cuppa();
        // TODO is it okay for no CuppaData at all?
        if (cuppaData != null) {
            for (CuppaPrediction pred: cuppaData.predictions()) {
                if (pred.snvPairwiseClassifier() == null) {
                    throw new IllegalStateException(String.format(message, "snvPairwiseClassifer"));
                }

                if (pred.genomicPositionClassifier() == null) {
                    throw new IllegalStateException(String.format(message, "genomicPositionClassifier"));
                }

                if (pred.featureClassifier() == null) {
                    throw new IllegalStateException(String.format(message, "featureClassifier"));
                }
            }
        }
    }

    @NotNull
    private static String concat(@NotNull Set<String> strings) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
