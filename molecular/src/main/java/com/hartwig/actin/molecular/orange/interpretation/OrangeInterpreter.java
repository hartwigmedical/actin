package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OrangeInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(OrangeInterpreter.class);

    static final String MICROSATELLITE_STABLE = "MSS";
    static final String MICROSATELLITE_UNSTABLE = "MSI";

    static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";
    static final String HOMOLOGOUS_REPAIR_UNKNOWN = "CANNOT_BE_DETERMINED";

    @NotNull
    private final OrangeEventExtractor eventExtractor;
    @NotNull
    private final OrangeEvidenceFactory evidenceFactory;

    @NotNull
    public static OrangeInterpreter fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeInterpreter(OrangeEventExtractor.fromServeRecords(records), OrangeEvidenceFactory.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeInterpreter(@NotNull final OrangeEventExtractor eventExtractor, @NotNull final OrangeEvidenceFactory evidenceFactory) {
        this.eventExtractor = eventExtractor;
        this.evidenceFactory = evidenceFactory;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        OrangeEventExtraction extraction = eventExtractor.extract(record);

        return ImmutableMolecularRecord.builder()
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.date())
                .hasReliableQuality(record.hasReliableQuality())
                .mutations(extraction.mutations())
                .activatedGenes(extraction.activatedGenes())
                .inactivatedGenes(extraction.inactivatedGenes())
                .amplifiedGenes(extraction.amplifiedGenes())
                .wildtypeGenes(extraction.wildtypeGenes())
                .fusions(extraction.fusions())
                .isMicrosatelliteUnstable(isMSI(record.microsatelliteStabilityStatus()))
                .isHomologousRepairDeficient(isHRD(record.homologousRepairStatus()))
                .tumorMutationalBurden(record.tumorMutationalBurden())
                .tumorMutationalLoad(record.tumorMutationalLoad())
                .actinSource("Erasmus MC")
                .actinTrials(evidenceFactory.createActinTrials(record.evidences()))
                .externalTrialSource("iClusion")
                .externalTrials(evidenceFactory.createExternalTrials(record.evidences()))
                .evidenceSource("CKB")
                .approvedResponsiveEvidence(evidenceFactory.createApprovedResponsiveEvidence(record.evidences()))
                .experimentalResponsiveEvidence(evidenceFactory.createExperimentalResponsiveEvidence(record.evidences()))
                .otherResponsiveEvidence(evidenceFactory.createOtherResponsiveEvidence(record.evidences()))
                .resistanceEvidence(evidenceFactory.createResistanceEvidence(record.evidences()))
                .build();
    }

    @Nullable
    private static Boolean isMSI(@NotNull String microsatelliteStatus) {
        if (microsatelliteStatus.equals(MICROSATELLITE_UNSTABLE)) {
            return true;
        } else if (microsatelliteStatus.equals(MICROSATELLITE_STABLE)) {
            return false;
        }

        LOGGER.warn("Cannot interpret microsatellite status '{}'", microsatelliteStatus);
        return null;
    }

    @Nullable
    private static Boolean isHRD(@NotNull String homologousRepairStatus) {
        switch (homologousRepairStatus) {
            case HOMOLOGOUS_REPAIR_DEFICIENT:
                return true;
            case HOMOLOGOUS_REPAIR_PROFICIENT:
                return false;
            case HOMOLOGOUS_REPAIR_UNKNOWN:
                return null;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", homologousRepairStatus);
        return null;
    }
}
