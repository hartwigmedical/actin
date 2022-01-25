package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestOrangeDataFactory {

    private TestOrangeDataFactory() {
    }

    @NotNull
    public static OrangeRecord createMinimalTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .hasReliableQuality(false)
                .microsatelliteStabilityStatus(Strings.EMPTY)
                .homologousRepairStatus(Strings.EMPTY)
                .tumorMutationalBurden(8D)
                .tumorMutationalLoad(100)
                .build();
    }

    @NotNull
    public static OrangeRecord createProperTestMolecularRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .date(LocalDate.of(2022, 1, 20))
                .hasReliableQuality(true)
                .microsatelliteStabilityStatus("MSS")
                .homologousRepairStatus("HR_PROFICIENT")
                .evidences(createTestEvidences())
                .build();
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder generalEvidenceBuilder = ImmutableTreatmentEvidence.builder().addSources("CKB").reported(true);
        evidences.add(generalEvidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(generalEvidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(generalEvidenceBuilder.gene("PTEN")
                .event("partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        ImmutableTreatmentEvidence.Builder generalTrialBuilder = ImmutableTreatmentEvidence.builder().addSources("ICLUSION").reported(true);
        evidences.add(generalTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial X")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        ImmutableTreatmentEvidence.Builder actinTrialBuilder = ImmutableTreatmentEvidence.builder().addSources("ACTIN").reported(true);
        evidences.add(actinTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial Y")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        return evidences;
    }
}
