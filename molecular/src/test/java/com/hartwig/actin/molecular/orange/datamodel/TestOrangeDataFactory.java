package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.datamodel.ImmutablePredictedTumorOrigin;
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin;

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
                .predictedTumorOrigin(ImmutablePredictedTumorOrigin.builder().tumorType("Unknown").likelihood(0D).build())
                .microsatelliteStabilityStatus(Strings.EMPTY)
                .homologousRepairStatus(Strings.EMPTY)
                .tumorMutationalBurden(8D)
                .tumorMutationalLoad(100)
                .build();
    }

    @NotNull
    public static OrangeRecord createProperTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .date(LocalDate.of(2022, 1, 20))
                .hasReliableQuality(true)
                .predictedTumorOrigin(createTestPredictedTumorOrigin())
                .microsatelliteStabilityStatus("MSS")
                .homologousRepairStatus("HR_PROFICIENT")
                .evidences(createTestEvidences())
                .build();
    }

    @NotNull
    private static PredictedTumorOrigin createTestPredictedTumorOrigin() {
        return ImmutablePredictedTumorOrigin.builder().tumorType("Melanoma").likelihood(0.996).build();
    }

    @NotNull
    private static List<TreatmentEvidence> createTestEvidences() {
        List<TreatmentEvidence> evidences = Lists.newArrayList();

        ImmutableTreatmentEvidence.Builder evidenceBuilder = ImmutableTreatmentEvidence.builder().addSources("CKB").reported(true);
        evidences.add(evidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .type(EvidenceType.HOTSPOT_MUTATION)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        evidences.add(evidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .type(EvidenceType.HOTSPOT_MUTATION)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        evidences.add(evidenceBuilder.gene("PTEN")
                .event("partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .type(EvidenceType.INACTIVATION)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .build());

        ImmutableTreatmentEvidence.Builder externalTrialBuilder =
                ImmutableTreatmentEvidence.builder().addSources("ICLUSION").reported(true);
        evidences.add(externalTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial X")
                .onLabel(true)
                .type(EvidenceType.HOTSPOT_MUTATION)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        ImmutableTreatmentEvidence.Builder actinTrialBuilder = ImmutableTreatmentEvidence.builder().addSources("ACTIN").reported(true);
        evidences.add(actinTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial Y")
                .onLabel(true)
                .type(EvidenceType.HOTSPOT_MUTATION)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .build());

        return evidences;
    }
}
