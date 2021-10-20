package com.hartwig.actin.molecular.datamodel;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestMolecularDataFactory {

    private TestMolecularDataFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .hasReliableQuality(true)
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .genomicTreatmentEvidences(createTestGenomicTreatmentEvidences())
                .build();
    }

    @NotNull
    private static List<GenomicTreatmentEvidence> createTestGenomicTreatmentEvidences() {
        List<GenomicTreatmentEvidence> genomicTreatmentEvidences = Lists.newArrayList();

        genomicTreatmentEvidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .genomicEvent("BRAF p.V600E")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        return genomicTreatmentEvidences;
    }
}
