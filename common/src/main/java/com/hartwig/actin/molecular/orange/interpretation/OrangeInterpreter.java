package com.hartwig.actin.molecular.orange.interpretation;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OrangeInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(OrangeInterpreter.class);

    static final String MICROSATELLITE_STABLE = "MSS";
    static final String MICROSATELLITE_UNSTABLE = "MSI";

    static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";

    private OrangeInterpreter() {
    }

    @NotNull
    public static MolecularRecord interpret(@NotNull OrangeRecord record) {
        OrangeEventExtraction extraction = OrangeEventExtractor.extract(record);

        Boolean isMSI = isMSI(record.microsatelliteStabilityStatus());
        Boolean isHRD = isHRD(record.homologousRepairStatus());

        // TODO This should flow from ACTIN evidence.
        MolecularEvidence evidence = overwriteWithSignatures(OrangeEvidenceFactory.create(record),
                isMSI,
                isHRD,
                record.tumorMutationalBurden(),
                record.tumorMutationalLoad());

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
                .isMicrosatelliteUnstable(isMSI)
                .isHomologousRepairDeficient(isHRD)
                .tumorMutationalBurden(record.tumorMutationalBurden())
                .tumorMutationalLoad(record.tumorMutationalLoad())
                .evidence(evidence)
                .build();
    }

    @NotNull
    private static MolecularEvidence overwriteWithSignatures(@NotNull MolecularEvidence evidence, @Nullable Boolean isMSI,
            @Nullable Boolean isHRD, double tumorMutationalBurden, int tumorMutationalLoad) {
        Multimap<String, String> actinTrialEvidence = ArrayListMultimap.create();
        actinTrialEvidence.putAll(evidence.actinTrialEvidence());

        if (isMSI != null && isMSI) {
            actinTrialEvidence.put("MSI", Strings.EMPTY);
        }

        if (isHRD != null && isHRD) {
            actinTrialEvidence.put("HR deficiency", Strings.EMPTY);
        }

        if (tumorMutationalBurden >= 10D) {
            actinTrialEvidence.put("High TMB", Strings.EMPTY);
        }

        if (tumorMutationalLoad >= 140) {
            actinTrialEvidence.put("High TML", Strings.EMPTY);
        }

        return ImmutableMolecularEvidence.builder().from(evidence).actinTrialEvidence(actinTrialEvidence).build();
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
        if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_DEFICIENT)) {
            return true;
        } else if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_PROFICIENT)) {
            return false;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", homologousRepairStatus);
        return null;
    }
}
