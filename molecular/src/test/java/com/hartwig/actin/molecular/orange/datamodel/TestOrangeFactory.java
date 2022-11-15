package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.chord.ImmutableChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaPrediction;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.ImmutableCuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacHlaAllele;
import com.hartwig.actin.molecular.orange.datamodel.lilac.ImmutableLilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.FusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachEntry;
import com.hartwig.actin.molecular.orange.datamodel.peach.ImmutablePeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.VariantHotspot;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterEntry;
import com.hartwig.actin.molecular.orange.datamodel.virus.ImmutableVirusInterpreterRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestOrangeFactory {

    private TestOrangeFactory() {
    }

    @NotNull
    public static OrangeRecord createMinimalTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .experimentDate(LocalDate.of(2021, 5, 6))
                .purple(createMinimalTestPurpleRecord())
                .linx(ImmutableLinxRecord.builder().build())
                .peach(ImmutablePeachRecord.builder().build())
                .cuppa(ImmutableCuppaRecord.builder().build())
                .virusInterpreter(ImmutableVirusInterpreterRecord.builder().build())
                .lilac(ImmutableLilacRecord.builder().qc(Strings.EMPTY).build())
                .chord(ImmutableChordRecord.builder().hrStatus(Strings.EMPTY).build())
                .build();
    }

    @NotNull
    public static OrangeRecord createProperTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .purple(createTestPurpleRecord())
                .linx(createTestLinxRecord())
                .peach(createTestPeachRecord())
                .cuppa(createTestCuppaRecord())
                .virusInterpreter(createTestVirusInterpreterRecord())
                .lilac(createTestLilacRecord())
                .chord(ImmutableChordRecord.builder().hrStatus("HR_PROFICIENT").build())
                .build();
    }

    @NotNull
    private static PurpleRecord createMinimalTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .containsTumorCells(true)
                .purity(0.98)
                .ploidy(3.1)
                .hasSufficientQuality(true)
                .microsatelliteStabilityStatus("MSS")
                .tumorMutationalBurden(13D)
                .tumorMutationalLoad(189)
                .tumorMutationalLoadStatus("HIGH")
                .build();
    }

    @NotNull
    private static PurpleRecord createTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .from(createMinimalTestPurpleRecord())
                .addVariants(TestPurpleFactory.variantBuilder()
                        .gene("BRAF")
                        .addCanonicalEffects(PurpleVariantEffect.MISSENSE)
                        .canonicalHgvsProteinImpact("p.Val600Glu")
                        .canonicalHgvsCodingImpact("c.something")
                        .totalCopyNumber(6.0)
                        .alleleCopyNumber(4.1)
                        .hotspot(VariantHotspot.HOTSPOT)
                        .clonalLikelihood(0.98)
                        .driverLikelihood(1)
                        .biallelic(false)
                        .build())
                .addGainsLosses(TestPurpleFactory.gainLossBuilder()
                        .gene("MYC")
                        .interpretation(GainLossInterpretation.FULL_GAIN)
                        .minCopies(38)
                        .build())
                .addGainsLosses(TestPurpleFactory.gainLossBuilder()
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
    private static CuppaRecord createTestCuppaRecord() {
        return ImmutableCuppaRecord.builder()
                .addPredictions(ImmutableCuppaPrediction.builder().cancerType("Melanoma").likelihood(0.996).build())
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
    private static LilacRecord createTestLilacRecord() {
        return ImmutableLilacRecord.builder()
                .qc("PASS")
                .addAlleles(ImmutableLilacHlaAllele.builder()
                        .allele("A*01:01")
                        .tumorCopyNumber(1.2)
                        .somaticMissense(0D)
                        .somaticNonsenseOrFrameshift(0D)
                        .somaticSplice(0D)
                        .somaticInframeIndel(0D)
                        .build())
                .build();
    }
}
