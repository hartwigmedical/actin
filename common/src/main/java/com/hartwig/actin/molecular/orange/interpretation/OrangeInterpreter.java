package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class OrangeInterpreter {

    private static final Logger LOGGER = LogManager.getLogger(OrangeInterpreter.class);

    private static final String MICROSATELLITE_STABLE = "MSS";
    private static final String MICROSATELLITE_UNSTABLE = "MSI";

    private static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    private static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";

    private OrangeInterpreter() {
    }

    @NotNull
    public static MolecularRecord interpret(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.date())
                .hasReliableQuality(record.hasReliableQuality())
                .isMicrosatelliteUnstable(isMSI(record.microsatelliteStabilityStatus()))
                .isHomologousRepairDeficient(isHRD(record.homologousRepairStatus()))
                .tumorMutationalBurden(record.tumorMutationalBurden())
                .tumorMutationalLoad(record.tumorMutationalLoad())
                .evidence(OrangeEvidenceFactory.create(record))
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
        if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_DEFICIENT)) {
            return true;
        } else if (homologousRepairStatus.equals(HOMOLOGOUS_REPAIR_PROFICIENT)) {
            return false;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", homologousRepairStatus);
        return null;
    }
}
