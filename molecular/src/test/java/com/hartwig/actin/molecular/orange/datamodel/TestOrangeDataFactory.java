package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceDirection;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceLevel;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestOrangeDataFactory {

    private TestOrangeDataFactory() {
    }

    @NotNull
    public static OrangeRecord createMinimalTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .reportDate(LocalDate.of(2021, 5, 6))
                .purple(createMinimalTestPurpleRecord())
                .linx(ImmutableLinxRecord.builder().build())
                .peach(ImmutablePeachRecord.builder().build())
                .cuppa(ImmutableCuppaRecord.builder().predictedCancerType("Unknown").bestPredictionLikelihood(0D).build())
                .virusInterpreter(ImmutableVirusInterpreterRecord.builder().build())
                .chord(ImmutableChordRecord.builder().hrStatus(Strings.EMPTY).build())
                .protect(ImmutableProtectRecord.builder().build())
                .build();
    }

    @NotNull
    public static OrangeRecord createProperTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .purple(createTestPurpleRecord())
                .linx(createTestLinxRecord())
                .peach(createTestPeachRecord())
                .cuppa(ImmutableCuppaRecord.builder().predictedCancerType("Melanoma").bestPredictionLikelihood(0.996).build())
                .virusInterpreter(createTestVirusInterpreterRecord())
                .chord(ImmutableChordRecord.builder().hrStatus("HR_PROFICIENT").build())
                .protect(createTestProtectRecord())
                .build();
    }

    @NotNull
    private static PurpleRecord createMinimalTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .hasReliableQuality(true)
                .purity(0.98)
                .hasReliablePurity(true)
                .microsatelliteStabilityStatus("MSS")
                .tumorMutationalBurden(13D)
                .tumorMutationalLoad(189)
                .build();
    }

    @NotNull
    private static PurpleRecord createTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .from(createMinimalTestPurpleRecord())
                .addVariants(ImmutablePurpleVariant.builder()
                        .gene("BRAF")
                        .hgvsProteinImpact("p.Val600Glu")
                        .hgvsCodingImpact("c.something")
                        .effect("missense_variant")
                        .alleleCopyNumber(4.1)
                        .totalCopyNumber(6.0)
                        .hotspot(VariantHotspot.HOTSPOT)
                        .biallelic(false)
                        .driverLikelihood(1)
                        .clonalLikelihood(0.98)
                        .build())
                .addGainsLosses(ImmutablePurpleGainLoss.builder()
                        .gene("MYC")
                        .interpretation(GainLossInterpretation.FULL_GAIN)
                        .minCopies(38)
                        .build())
                .addGainsLosses(ImmutablePurpleGainLoss.builder()
                        .gene("PTEN")
                        .interpretation(GainLossInterpretation.PARTIAL_LOSS)
                        .minCopies(0)
                        .build())
                .build();
    }

    @NotNull
    private static LinxRecord createTestLinxRecord() {
        return ImmutableLinxRecord.builder()
                .addFusions(ImmutableLinxFusion.builder()
                        .type(FusionType.KNOWN_PAIR)
                        .geneStart("EML4")
                        .geneContextStart("Exon 2")
                        .geneEnd("ALK")
                        .geneContextEnd("Exon 4")
                        .driverLikelihood(FusionDriverLikelihood.HIGH)
                        .build())
                .addHomozygousDisruptedGenes("TP53")
                .addDisruptions(ImmutableLinxDisruption.builder()
                        .gene("RB1")
                        .type("DEL")
                        .junctionCopyNumber(0.8)
                        .undisruptedCopyNumber(2.1)
                        .range("Intron 1 downstream")
                        .build())
                .build();
    }

    @NotNull
    private static PeachRecord createTestPeachRecord() {
        return ImmutablePeachRecord.builder()
                .addEntries(ImmutablePeachEntry.builder().gene("DPYD").haplotype("1* HOM").function("Normal function").build())
                .build();
    }

    @NotNull
    private static VirusInterpreterRecord createTestVirusInterpreterRecord() {
        return ImmutableVirusInterpreterRecord.builder()
                .addEntries(ImmutableVirusInterpreterEntry.builder()
                        .name("Human papillomavirus type 16")
                        .interpretation("HPV")
                        .integrations(3)
                        .driverLikelihood(VirusDriverLikelihood.HIGH)
                        .build())
                .build();
    }

    @NotNull
    private static ProtectRecord createTestProtectRecord() {
        List<ProtectEvidence> evidences = Lists.newArrayList();

        ImmutableProtectSource.Builder evidenceSourceBuilder = ImmutableProtectSource.builder().name("CKB").event(Strings.EMPTY);
        ImmutableProtectEvidence.Builder evidenceBuilder = ImmutableProtectEvidence.builder().reported(true);
        evidences.add(evidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Vemurafenib")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(evidenceSourceBuilder.type(EvidenceType.HOTSPOT_MUTATION).build())
                .build());

        evidences.add(evidenceBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Cetuximab")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .addSources(evidenceSourceBuilder.type(EvidenceType.HOTSPOT_MUTATION).build())
                .build());

        evidences.add(evidenceBuilder.gene("PTEN")
                .event("partial loss")
                .treatment("Everolimus")
                .onLabel(false)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESISTANT)
                .addSources(evidenceSourceBuilder.type(EvidenceType.INACTIVATION).build())
                .build());

        ImmutableProtectSource.Builder externalSourceBuilder = ImmutableProtectSource.builder().name("ICLUSION").event(Strings.EMPTY);
        ImmutableProtectEvidence.Builder externalTrialBuilder = ImmutableProtectEvidence.builder().reported(true);
        evidences.add(externalTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial X")
                .onLabel(true)
                .level(EvidenceLevel.B)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(externalSourceBuilder.type(EvidenceType.HOTSPOT_MUTATION).build())
                .build());

        ImmutableProtectSource.Builder actinSourceBuilder = ImmutableProtectSource.builder().name("ACTIN").event(Strings.EMPTY);
        ImmutableProtectEvidence.Builder actinTrialBuilder = ImmutableProtectEvidence.builder().reported(true);
        evidences.add(actinTrialBuilder.gene("BRAF")
                .event("p.Val600Glu")
                .treatment("Trial Y")
                .onLabel(true)
                .level(EvidenceLevel.A)
                .direction(EvidenceDirection.RESPONSIVE)
                .addSources(actinSourceBuilder.type(EvidenceType.HOTSPOT_MUTATION).build())
                .build());

        return ImmutableProtectRecord.builder().evidences(evidences).build();
    }
}
