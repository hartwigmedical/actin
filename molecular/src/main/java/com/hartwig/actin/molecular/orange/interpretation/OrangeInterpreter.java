package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.PredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
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
    private final OrangeEventMapper eventMapper;
    @NotNull
    private final OrangeEvidenceFactory evidenceFactory;

    @NotNull
    public static OrangeInterpreter fromServeRecords(@NotNull List<ServeRecord> records) {
        return new OrangeInterpreter(OrangeEventMapper.fromServeRecords(records), OrangeEvidenceFactory.fromServeRecords(records));
    }

    @VisibleForTesting
    OrangeInterpreter(@NotNull final OrangeEventMapper eventMapper, @NotNull final OrangeEvidenceFactory evidenceFactory) {
        this.eventMapper = eventMapper;
        this.evidenceFactory = evidenceFactory;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.reportDate())
                .hasReliableQuality(record.purple().hasReliableQuality())
                .characteristics(extractCharacteristics(record))
                .drivers(ImmutableMolecularDrivers.builder().build())
                .pharmaco(Lists.newArrayList())
                .evidence(extractEvidence(record))
                .mappedEvents(eventMapper.map(record.protect()))
                .build();
    }

    @NotNull
    private static MolecularCharacteristics extractCharacteristics(@NotNull OrangeRecord record) {
        PredictedTumorOrigin predictedTumorOrigin = ImmutablePredictedTumorOrigin.builder()
                .tumorType(record.cuppa().predictedCancerType())
                .likelihood(record.cuppa().bestPredictionLikelihood())
                .build();

        PurpleRecord purple = record.purple();
        ChordRecord chord = record.chord();
        return ImmutableMolecularCharacteristics.builder()
                .purity(purple.purity())
                .hasReliablePurity(purple.hasReliablePurity())
                .predictedTumorOrigin(predictedTumorOrigin)
                .isMicrosatelliteUnstable(isMSI(purple.microsatelliteStabilityStatus()))
                .isHomologousRepairDeficient(isHRD(chord.hrStatus()))
                .tumorMutationalBurden(purple.tumorMutationalBurden())
                .tumorMutationalLoad(purple.tumorMutationalLoad())
                .build();
    }

    @NotNull
    private MolecularEvidence extractEvidence(@NotNull OrangeRecord record) {
        ProtectRecord protect = record.protect();
        return ImmutableMolecularEvidence.builder()
                .actinSource("Erasmus MC")
                .actinTrials(evidenceFactory.createActinTrials(protect.evidences()))
                .externalTrialSource("iClusion")
                .externalTrials(evidenceFactory.createExternalTrials(protect.evidences()))
                .evidenceSource("CKB")
                .approvedResponsiveEvidence(evidenceFactory.createApprovedResponsiveEvidence(protect.evidences()))
                .experimentalResponsiveEvidence(evidenceFactory.createExperimentalResponsiveEvidence(protect.evidences()))
                .otherResponsiveEvidence(evidenceFactory.createOtherResponsiveEvidence(protect.evidences()))
                .resistanceEvidence(evidenceFactory.createResistanceEvidence(protect.evidences()))
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
