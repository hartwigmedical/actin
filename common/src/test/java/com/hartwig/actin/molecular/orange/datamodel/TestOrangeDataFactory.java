package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestOrangeDataFactory {

    private static final LocalDate TODAY = LocalDate.now();

    private static final int DAYS_SINCE_ORANGE_ANALYSIS = 5;

    private TestOrangeDataFactory() {
    }

    @NotNull
    public static OrangeRecord createMinimalTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .hasReliableQuality(true)
                .microsatelliteStabilityStatus("MSS")
                .homologousRepairStatus("HR_PROFICIENT")
                .tumorMutationalBurden(0D)
                .tumorMutationalLoad(0)
                .build();
    }

    @NotNull
    public static OrangeRecord createProperTestMolecularRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .date(TODAY.minusDays(DAYS_SINCE_ORANGE_ANALYSIS))
                .tumorMutationalBurden(13.71)
                .tumorMutationalLoad(185)
                .evidences(createTestEvidences())
                .build();
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder ckbBuilder = ImmutableTreatmentEvidence.builder().addSources("CKB");
        ImmutableTreatmentEvidence.Builder iclusionBuilder = ImmutableTreatmentEvidence.builder().addSources("ICLUSION");

        evidences.add(ckbBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ckbBuilder.event("BRAF")
                .event("p.Val600Glu")
                .treatment("Dabrafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(ckbBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(ckbBuilder.gene("PTEN")
                .event("partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(iclusionBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial X")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        return evidences;
    }
}
