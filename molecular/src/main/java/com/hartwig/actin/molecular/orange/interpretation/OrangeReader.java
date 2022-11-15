package com.hartwig.actin.molecular.orange.interpretation;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.filter.GeneFilter;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class OrangeReader {

    @NotNull
    private final GeneFilter geneFilter;

    public OrangeReader(@NotNull final GeneFilter geneFilter) {
        this.geneFilter = geneFilter;
    }

    @NotNull
    public MolecularRecord read(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .patientId(toPatientId(record.sampleId()))
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .date(record.experimentDate())
                .evidenceSource(Strings.EMPTY)
                .externalTrialSource(Strings.EMPTY)
                .containsTumorCells(record.purple().containsTumorCells())
                .hasSufficientQuality(record.purple().hasSufficientQuality())
                .characteristics(CharacteristicsExtraction.extract(record))
                .drivers(DriverExtraction.extract(record))
                .immunology(ImmunologyExtraction.extract(record))
                .pharmaco(PharmacoExtraction.extract(record))
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
