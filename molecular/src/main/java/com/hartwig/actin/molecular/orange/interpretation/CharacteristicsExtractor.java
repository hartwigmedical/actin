package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CharacteristicsExtractor {

    private static final Logger LOGGER = LogManager.getLogger(CharacteristicsExtractor.class);

    static final String MICROSATELLITE_STABLE = "MSS";
    static final String MICROSATELLITE_UNSTABLE = "MSI";

    static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HR_DEFICIENT";
    static final String HOMOLOGOUS_REPAIR_PROFICIENT = "HR_PROFICIENT";
    static final String HOMOLOGOUS_REPAIR_UNKNOWN = "CANNOT_BE_DETERMINED";

    static final String TUMOR_STATUS_HIGH = "HIGH";
    static final String TUMOR_STATUS_LOW = "LOW";
    static final String TUMOR_STATUS_UNKNOWN = "UNKNOWN";

    static final double HIGH_TMB_CUTOFF = 10;

    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public CharacteristicsExtractor(@NotNull final EvidenceDatabase evidenceDatabase) {
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public MolecularCharacteristics extract(@NotNull OrangeRecord record) {
        CuppaPrediction best = findBestCuppaPrediction(record.cuppa().predictions());
        PredictedTumorOrigin predictedTumorOrigin = best != null
                ? ImmutablePredictedTumorOrigin.builder().tumorType(best.cancerType()).likelihood(best.likelihood()).build()
                : null;

        PurpleRecord purple = record.purple();

        // TODO: Make TMB interpretation inside ORANGE.

        Boolean isMicrosatelliteUnstable = isMSI(purple.microsatelliteStabilityStatus());
        Boolean isHomologousRepairDeficient = isHRD(record.chord().hrStatus());
        Boolean hasHighTumorMutationalBurden = purple.tumorMutationalBurden() >= HIGH_TMB_CUTOFF;
        Boolean hasHighTumorMutationalLoad = hasHighStatus(purple.tumorMutationalLoadStatus());

        return ImmutableMolecularCharacteristics.builder()
                .purity(purple.purity())
                .ploidy(purple.ploidy())
                .predictedTumorOrigin(predictedTumorOrigin)
                .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                .microsatelliteEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForMicrosatelliteStatus(
                        isMicrosatelliteUnstable)))
                .isHomologousRepairDeficient(isHomologousRepairDeficient)
                .homologousRepairEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomologousRepairStatus(
                        isHomologousRepairDeficient)))
                .tumorMutationalBurden(purple.tumorMutationalBurden())
                .hasHighTumorMutationalBurden(hasHighTumorMutationalBurden)
                .tumorMutationalBurdenEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalBurdenStatus(
                        hasHighTumorMutationalBurden)))
                .tumorMutationalLoad(purple.tumorMutationalLoad())
                .hasHighTumorMutationalLoad(hasHighTumorMutationalLoad)
                .tumorMutationalLoadEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalLoadStatus(
                        hasHighTumorMutationalLoad)))
                .build();
    }

    @Nullable
    private static CuppaPrediction findBestCuppaPrediction(@NotNull Set<CuppaPrediction> predictions) {
        CuppaPrediction best = null;
        for (CuppaPrediction prediction : predictions) {
            if (best == null || prediction.likelihood() > best.likelihood()) {
                best = prediction;
            }
        }
        return best;
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

    @Nullable
    private static Boolean hasHighStatus(@NotNull String tumorMutationalStatus) {
        switch (tumorMutationalStatus) {
            case TUMOR_STATUS_HIGH: {
                return true;
            }
            case TUMOR_STATUS_LOW: {
                return false;
            }
            case TUMOR_STATUS_UNKNOWN: {
                return null;
            }
        }

        LOGGER.warn("Cannot interpret tumor mutational status: {}", tumorMutationalStatus);
        return null;
    }
}
