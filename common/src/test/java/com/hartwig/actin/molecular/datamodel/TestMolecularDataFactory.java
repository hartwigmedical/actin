package com.hartwig.actin.molecular.datamodel;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.TestDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestMolecularDataFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_MOLECULAR_ANALYSIS = 5;

    private TestMolecularDataFactory() {
    }

    @NotNull
    public static MolecularRecord createMinimalTestMolecularRecord() {
        return ImmutableMolecularRecord.builder().sampleId(TestDataFactory.TEST_SAMPLE).type(ExperimentType.WGS)
                .hasReliableQuality(true)
                .tumorMutationalBurden(0D)
                .tumorMutationalLoad(0)
                .build();
    }

    @NotNull
    public static MolecularRecord createProperTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(createMinimalTestMolecularRecord())
                .date(TODAY.minusDays(DAYS_SINCE_MOLECULAR_ANALYSIS))
                .configuredPrimaryTumorDoids(createTestConfiguredPrimaryTumorDoids())
                .isMicrosatelliteUnstable(false)
                .isHomologousRepairDeficient(false)
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(189)
                .evidences(createTestEvidences())
                .build();
    }

    @NotNull
    private static Set<String> createTestConfiguredPrimaryTumorDoids() {
        return Sets.newHashSet("8923");
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder ckbBuilder = ImmutableTreatmentEvidence.builder().addSources("CKB");
        ImmutableTreatmentEvidence.Builder iclusionBuilder = ImmutableTreatmentEvidence.builder().addSources("ICLUSION");

        evidences.add(ckbBuilder.genomicEvent("BRAF p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ckbBuilder.genomicEvent("BRAF p.Val600Glu")
                .treatment("Dabrafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ckbBuilder.genomicEvent("BRAF p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(ckbBuilder.genomicEvent("PTEN partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(iclusionBuilder.genomicEvent("BRAF p.Val600Glu")
                .treatment("Trial X")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        return evidences;
    }
}
