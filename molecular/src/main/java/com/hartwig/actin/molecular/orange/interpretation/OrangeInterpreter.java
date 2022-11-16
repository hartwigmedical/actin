package com.hartwig.actin.molecular.orange.interpretation;

import com.google.common.annotations.VisibleForTesting;
import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceAnnotator;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public class OrangeInterpreter {

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceAnnotator evidenceAnnotator;

    public OrangeInterpreter(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceAnnotator evidenceAnnotator) {
        this.geneFilter = geneFilter;
        this.evidenceAnnotator = evidenceAnnotator;
    }

    @NotNull
    public MolecularRecord interpret(@NotNull OrangeRecord record) {
        return ImmutableMolecularRecord.builder()
                .patientId(toPatientId(record.sampleId()))
                .sampleId(record.sampleId())
                .type(ExperimentType.WGS)
                .refGenomeVersion(RefGenomeVersion.V37)
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
