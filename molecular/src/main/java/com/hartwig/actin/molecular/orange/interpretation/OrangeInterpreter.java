package com.hartwig.actin.molecular.orange.interpretation;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRefGenomeVersion;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;

import org.jetbrains.annotations.NotNull;

public class OrangeInterpreter {

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public OrangeInterpreter(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        DriverExtractor driverExtractor = DriverExtractor.create(geneFilter, evidenceDatabase);
        CharacteristicsExtractor characteristicsExtractor = new CharacteristicsExtractor(evidenceDatabase);

        return ImmutableMolecularRecord.builder()
                .patientId(toPatientId(record.sampleId()))
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .refGenomeVersion(determineRefGenomeVersion(record.refGenomeVersion()))
                .date(record.experimentDate())
                .evidenceSource(ActionabilityConstants.EVIDENCE_SOURCE.display())
                .externalTrialSource(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE.display())
                .containsTumorCells(record.purple().fit().hasReliablePurity())
                .hasSufficientQuality(record.purple().fit().hasReliableQuality())
                .characteristics(characteristicsExtractor.extract(record))
                .drivers(driverExtractor.extract(record))
                .immunology(ImmunologyExtraction.extract(record))
                .pharmaco(PharmacoExtraction.extract(record))
                .build();
    }

    @NotNull
    static RefGenomeVersion determineRefGenomeVersion(@NotNull OrangeRefGenomeVersion refGenomeVersion) {
        switch (refGenomeVersion) {
            case V37: {
                return RefGenomeVersion.V37;
            } case V38: {
                return RefGenomeVersion.V38;
            }
        }

        throw new IllegalStateException("Could not determine ref genome version from: " + refGenomeVersion);
    }

    @VisibleForTesting
    @NotNull
    static String toPatientId(@NotNull String sampleId) {
        if (sampleId.length() < 12) {
            throw new IllegalArgumentException("Cannot convert sampleId to patientId: " + sampleId);
        }
        return sampleId.substring(0, 12);
    }
}
