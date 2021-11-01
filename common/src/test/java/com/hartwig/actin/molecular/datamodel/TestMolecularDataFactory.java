package com.hartwig.actin.molecular.datamodel;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
                .configuredPrimaryTumorDoids(createTestConfiguredPrimaryTumorDoids())
                .evidences(createTestEvidences())
                .build();
    }

    @NotNull
    private static Set<String> createTestConfiguredPrimaryTumorDoids() {
        return Sets.newHashSet("8923");
    }

    @NotNull
    private static List<MolecularTreatmentEvidence> createTestEvidences() {
        List<MolecularTreatmentEvidence> evidences = Lists.newArrayList();

        evidences.add(ImmutableMolecularTreatmentEvidence.builder()
                .genomicEvent("BRAF p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ImmutableMolecularTreatmentEvidence.builder()
                .genomicEvent("BRAF p.Val600Glu")
                .treatment("Dabrafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ImmutableMolecularTreatmentEvidence.builder()
                .genomicEvent("BRAF p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(ImmutableMolecularTreatmentEvidence.builder()
                .genomicEvent("PTEN partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        return evidences;
    }
}
