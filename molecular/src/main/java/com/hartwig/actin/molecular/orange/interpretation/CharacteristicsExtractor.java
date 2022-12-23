package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordStatus;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleMicrosatelliteStatus;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleTumorMutationalStatus;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class CharacteristicsExtractor {

    private static final Logger LOGGER = LogManager.getLogger(CharacteristicsExtractor.class);

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

        Boolean isMicrosatelliteUnstable = isMSI(purple.characteristics().microsatelliteStatus());
        Boolean isHomologousRepairDeficient = isHRD(record.chord().hrStatus());
        Boolean hasHighTumorMutationalBurden = hasHighStatus(purple.characteristics().tumorMutationalBurdenStatus());
        Boolean hasHighTumorMutationalLoad = hasHighStatus(purple.characteristics().tumorMutationalLoadStatus());

        return ImmutableMolecularCharacteristics.builder()
                .purity(purple.fit().purity())
                .ploidy(purple.fit().ploidy())
                .predictedTumorOrigin(predictedTumorOrigin)
                .isMicrosatelliteUnstable(isMicrosatelliteUnstable)
                .microsatelliteEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForMicrosatelliteStatus(
                        isMicrosatelliteUnstable)))
                .isHomologousRepairDeficient(isHomologousRepairDeficient)
                .homologousRepairEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomologousRepairStatus(
                        isHomologousRepairDeficient)))
                .tumorMutationalBurden(purple.characteristics().tumorMutationalBurdenPerMb())
                .hasHighTumorMutationalBurden(hasHighTumorMutationalBurden)
                .tumorMutationalBurdenEvidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForTumorMutationalBurdenStatus(
                        hasHighTumorMutationalBurden)))
                .tumorMutationalLoad(purple.characteristics().tumorMutationalLoad())
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
    private static Boolean isMSI(@NotNull PurpleMicrosatelliteStatus microsatelliteStatus) {
        switch (microsatelliteStatus) {
            case MSI: {
                return true;
            }
            case MSS: {
                return false;
            }
            case UNKNOWN: {
                return null;
            }
        }

        LOGGER.warn("Cannot interpret microsatellite status '{}'", microsatelliteStatus);
        return null;
    }

    @Nullable
    private static Boolean isHRD(@NotNull ChordStatus hrStatus) {
        switch (hrStatus) {
            case HR_DEFICIENT:
                return true;
            case HR_PROFICIENT:
                return false;
            case UNKNOWN:
            case CANNOT_BE_DETERMINED:
                return null;
        }

        LOGGER.warn("Cannot interpret homologous repair status '{}'", hrStatus);
        return null;
    }

    @Nullable
    private static Boolean hasHighStatus(@NotNull PurpleTumorMutationalStatus tumorMutationalStatus) {
        switch (tumorMutationalStatus) {
            case HIGH: {
                return true;
            }
            case LOW: {
                return false;
            }
            case UNKNOWN: {
                return null;
            }
        }

        LOGGER.warn("Cannot interpret tumor mutational status: {}", tumorMutationalStatus);
        return null;
    }
}
