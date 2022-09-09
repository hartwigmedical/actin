package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.jetbrains.annotations.NotNull;

public class OrangeInterpreter {

    @NotNull
    private final EvidenceExtractor evidenceExtractor;

    @NotNull
    public static OrangeInterpreter create(@NotNull List<ExternalTrialMapping> mappings) {
        return new OrangeInterpreter(EvidenceExtractor.extract(mappings));
    }

    @VisibleForTesting
    OrangeInterpreter(@NotNull final EvidenceExtractor evidenceExtractor) {
        this.evidenceExtractor = evidenceExtractor;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .patientId(toPatientId(record.sampleId()))
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.experimentDate())
                .containsTumorCells(record.purple().containsTumorCells())
                .hasSufficientQuality(record.purple().hasSufficientQuality())
                .characteristics(CharacteristicsExtraction.extract(record))
                .drivers(DriverExtraction.extract(record))
                .immunology(ImmunologyExtraction.extract(record))
                .pharmaco(PharmacoExtraction.extract(record))
                .wildTypeGenes(WildTypeExtraction.extract(record))
                .evidence(evidenceExtractor.extract(record))
                .build();
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
