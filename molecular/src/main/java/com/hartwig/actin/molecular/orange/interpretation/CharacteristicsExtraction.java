package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CharacteristicsExtraction {

    private static final Logger LOGGER = LogManager.getLogger(CharacteristicsExtraction.class);

    static final String MICROSATELLITE_STABLE = "MSS";
    static final String MICROSATELLITE_UNSTABLE = "MSI";

    static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";
    static final String HOMOLOGOUS_REPAIR_UNKNOWN = "CANNOT_BE_DETERMINED";

    private CharacteristicsExtraction() {
    }

    @NotNull
    public static MolecularCharacteristics extract(@NotNull OrangeRecord record) {
        PredictedTumorOrigin predictedTumorOrigin = ImmutablePredictedTumorOrigin.builder()
                .tumorType(record.cuppa().predictedCancerType())
                .likelihood(record.cuppa().bestPredictionLikelihood())
                .build();

        PurpleRecord purple = record.purple();
        return ImmutableMolecularCharacteristics.builder()
                .purity(purple.purity())
                .hasReliablePurity(purple.hasReliablePurity())
                .predictedTumorOrigin(predictedTumorOrigin)
                .isMicrosatelliteUnstable(isMSI(purple.microsatelliteStabilityStatus()))
                .isHomologousRepairDeficient(isHRD(record.chord().hrStatus()))
                .tumorMutationalBurden(purple.tumorMutationalBurden())
                .tumorMutationalLoad(purple.tumorMutationalLoad())
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
    private static Boolean isHRD(@NotNull String hrStatus) {
        switch (hrStatus) {
            case HOMOLOGOUS_REPAIR_DEFICIENT:
                return true;
            case HOMOLOGOUS_REPAIR_PROFICIENT:
                return false;
            case HOMOLOGOUS_REPAIR_UNKNOWN:
                return null;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", hrStatus);
        return null;
    }
}
