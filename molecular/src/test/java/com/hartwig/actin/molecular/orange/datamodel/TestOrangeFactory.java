package com.hartwig.actin.molecular.orange.datamodel;

import static com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory.purpleQCBuilder;

import java.time.LocalDate;
import java.util.Collections;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.TestCuppaFactory;
import com.hartwig.actin.molecular.orange.datamodel.lilac.TestLilacFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.hmftools.datamodel.chord.ChordRecord;
import com.hartwig.hmftools.datamodel.chord.ChordStatus;
import com.hartwig.hmftools.datamodel.chord.ImmutableChordRecord;
import com.hartwig.hmftools.datamodel.cuppa.CuppaData;
import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaData;
import com.hartwig.hmftools.datamodel.flagstat.ImmutableFlagstat;
import com.hartwig.hmftools.datamodel.hla.ImmutableLilacRecord;
import com.hartwig.hmftools.datamodel.hla.LilacRecord;
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType;
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType;
import com.hartwig.hmftools.datamodel.linx.LinxFusionType;
import com.hartwig.hmftools.datamodel.linx.LinxRecord;
import com.hartwig.hmftools.datamodel.metrics.ImmutableWGSMetrics;
import com.hartwig.hmftools.datamodel.orange.ExperimentType;
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangePlots;
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord;
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeSample;
import com.hartwig.hmftools.datamodel.orange.OrangePlots;
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion;
import com.hartwig.hmftools.datamodel.orange.OrangeSample;
import com.hartwig.hmftools.datamodel.peach.ImmutablePeachGenotype;
import com.hartwig.hmftools.datamodel.peach.PeachGenotype;
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.hmftools.datamodel.purple.Hotspot;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord;
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics;
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect;
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType;
import com.hartwig.hmftools.datamodel.purple.PurpleFit;
import com.hartwig.hmftools.datamodel.purple.PurpleLikelihoodMethod;
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleRecord;
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect;
import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterData;
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus;
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation;
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData;
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType;

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
                .experimentType(ExperimentType.WHOLE_GENOME)
                .refGenomeVersion(OrangeRefGenomeVersion.V37)
                .purple(createMinimalTestPurpleRecord())
                .linx(ImmutableLinxRecord.builder().build())
                .lilac(createMinimalTestLilacRecord())
                .tumorSample(createOrangeTumorSample())
                .plots(createOrangePlots())
                .build();
    }

    @NotNull
    private static PurpleRecord createMinimalTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .fit(TestPurpleFactory.fitBuilder()
                        .qc(purpleQCBuilder().status(Collections.singleton(PurpleQCStatus.FAIL_NO_TUMOR)).build())
                        .build())
                .characteristics(TestPurpleFactory.characteristicsBuilder().build())
                .build();
    }

    @NotNull
    private static LilacRecord createMinimalTestLilacRecord() {
        return ImmutableLilacRecord.builder().qc(Strings.EMPTY).build();
    }

    @NotNull
    public static OrangeRecord createProperTestOrangeRecord() {
        return ImmutableOrangeRecord.builder()
                .from(createMinimalTestOrangeRecord())
                .purple(createTestPurpleRecord())
                .linx(createTestLinxRecord())
                .addPeach(createTestPeachGenotype())
                .cuppa(createTestCuppaRecord())
                .virusInterpreter(createTestVirusInterpreterRecord())
                .lilac(createTestLilacRecord())
                .chord(createTestChordRecord())
                .build();
    }

    @NotNull
    private static PurpleRecord createTestPurpleRecord() {
        return ImmutablePurpleRecord.builder()
                .from(createMinimalTestPurpleRecord())
                .fit(createTestPurpleFit())
                .characteristics(createTestPurpleCharacteristics())
                .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                        .gene("BRAF")
                        .driver(PurpleDriverType.MUTATION)
                        .driverLikelihood(1D)
                        .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                        .isCanonical(false)
                        .build())
                .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                        .gene("MYC")
                        .driver(PurpleDriverType.AMP)
                        .driverLikelihood(1D)
                        .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                        .isCanonical(false)
                        .build())
                .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                        .gene("PTEN")
                        .driver(PurpleDriverType.DEL)
                        .driverLikelihood(1D)
                        .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                        .isCanonical(false)
                        .build())
                .addAllSomaticVariants(TestPurpleFactory.variantBuilder()
                        .reported(true)
                        .gene("BRAF")
                        .adjustedCopyNumber(6.0)
                        .variantCopyNumber(4.1)
                        .hotspot(Hotspot.HOTSPOT)
                        .subclonalLikelihood(0.02)
                        .biallelic(false)
                        .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                                .hgvsCodingImpact("c.something")
                                .hgvsProteinImpact("p.Val600Glu")
                                .spliceRegion(false)
                                .addEffects(PurpleVariantEffect.MISSENSE)
                                .codingEffect(PurpleCodingEffect.MISSENSE)
                                .build())
                        .build())
                .addAllSomaticGainsLosses(TestPurpleFactory.gainLossBuilder()
                        .gene("MYC")
                        .interpretation(CopyNumberInterpretation.FULL_GAIN)
                        .minCopies(38)
                        .maxCopies(40)
                        .build())
                .addAllSomaticGainsLosses(TestPurpleFactory.gainLossBuilder()
                        .gene("PTEN")
                        .interpretation(CopyNumberInterpretation.FULL_LOSS)
                        .minCopies(0)
                        .maxCopies(0)
                        .build())
                .build();
    }

    @NotNull
    private static PurpleFit createTestPurpleFit() {
        return TestPurpleFactory.fitBuilder()
                .hasSufficientQuality(true)
                .containsTumorCells(true)
                .purity(0.98)
                .ploidy(3.1)
                .qc(purpleQCBuilder().addStatus(PurpleQCStatus.PASS).build())
                .build();
    }

    @NotNull
    private static PurpleCharacteristics createTestPurpleCharacteristics() {
        return TestPurpleFactory.characteristicsBuilder()
                .microsatelliteStatus(PurpleMicrosatelliteStatus.MSS)
                .tumorMutationalBurdenPerMb(13D)
                .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.HIGH)
                .tumorMutationalLoad(189)
                .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.HIGH)
                .build();
    }

    @NotNull
    private static LinxRecord createTestLinxRecord() {
        return ImmutableLinxRecord.builder()
                .addAllSomaticStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(1).build())
                .addSomaticHomozygousDisruptions(TestLinxFactory.homozygousDisruptionBuilder().gene("TP53").build())
                .addAllSomaticBreakends(TestLinxFactory.breakendBuilder()
                        .reportedDisruption(true)
                        .svId(1)
                        .gene("RB1")
                        .type(LinxBreakendType.DEL)
                        .junctionCopyNumber(0.8)
                        .undisruptedCopyNumber(2.1)
                        .build())
                .addAllSomaticBreakends(TestLinxFactory.breakendBuilder()
                        .reportedDisruption(true)
                        .svId(1)
                        .gene("PTEN")
                        .type(LinxBreakendType.DEL)
                        .junctionCopyNumber(1D)
                        .undisruptedCopyNumber(1D)
                        .build())
                .addAllSomaticFusions(TestLinxFactory.fusionBuilder()
                        .reported(true)
                        .reportedType(LinxFusionType.KNOWN_PAIR)
                        .geneStart("EML4")
                        .fusedExonUp(2)
                        .geneEnd("ALK")
                        .fusedExonDown(4)
                        .likelihood(FusionLikelihoodType.HIGH)
                        .build())
                .build();
    }

    @NotNull
    private static PeachGenotype createTestPeachGenotype() {
        return ImmutablePeachGenotype.builder()
                .gene("DPYD")
                .haplotype("1* HOM")
                .function("Normal function")
                .linkedDrugs(Strings.EMPTY)
                .urlPrescriptionInfo(Strings.EMPTY)
                .panelVersion(Strings.EMPTY)
                .repoVersion(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static CuppaData createTestCuppaRecord() {
        return ImmutableCuppaData.builder()
                .addPredictions(TestCuppaFactory.builder()
                        .cancerType("Melanoma")
                        .likelihood(0.996)
                        .snvPairwiseClassifier(0.979)
                        .genomicPositionClassifier(0.99)
                        .featureClassifier(0.972)
                        .build())
                .simpleDups32To200B(0)
                .maxComplexSize(0)
                .telomericSGLs(0)
                .lineCount(0)
                .build();
    }

    @NotNull
    private static VirusInterpreterData createTestVirusInterpreterRecord() {
        return ImmutableVirusInterpreterData.builder()
                .addAllViruses(TestVirusInterpreterFactory.builder()
                        .reported(true)
                        .name("Human papillomavirus type 16")
                        .qcStatus(VirusBreakendQCStatus.NO_ABNORMALITIES)
                        .interpretation(VirusInterpretation.HPV)
                        .integrations(3)
                        .virusDriverLikelihoodType(VirusLikelihoodType.HIGH)
                        .build())
                .build();
    }

    @NotNull
    private static LilacRecord createTestLilacRecord() {
        return ImmutableLilacRecord.builder()
                .qc("PASS")
                .addAlleles(TestLilacFactory.builder()
                        .allele("A*01:01")
                        .tumorCopyNumber(1.2)
                        .somaticSynonymous(0D)
                        .refFragments(0)
                        .tumorFragments(0)
                        .rnaFragments(0)
                        .build())
                .build();
    }

    @NotNull
    private static ChordRecord createTestChordRecord() {
        return ImmutableChordRecord.builder()
                .hrStatus(ChordStatus.HR_PROFICIENT)
                .brca1Value(0D)
                .brca2Value(0D)
                .hrdValue(0D)
                .hrdType(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static OrangePlots createOrangePlots() {
        return ImmutableOrangePlots.builder()
                .sageTumorBQRPlot(Strings.EMPTY)
                .purpleInputPlot(Strings.EMPTY)
                .purpleFinalCircosPlot(Strings.EMPTY)
                .purpleClonalityPlot(Strings.EMPTY)
                .purpleCopyNumberPlot(Strings.EMPTY)
                .purpleVariantCopyNumberPlot(Strings.EMPTY)
                .purplePurityRangePlot(Strings.EMPTY)
                .build();
    }

    @NotNull
    private static OrangeSample createOrangeTumorSample() {
        return ImmutableOrangeSample.builder()
                .flagstat(ImmutableFlagstat.builder()
                        .uniqueReadCount(0L)
                        .secondaryCount(0L)
                        .supplementaryCount(0L)
                        .mappedProportion(0L)
                        .build())
                .metrics(ImmutableWGSMetrics.builder()
                        .meanCoverage(0D)
                        .sdCoverage(0D)
                        .medianCoverage(0)
                        .madCoverage(0)
                        .pctExcMapQ(0D)
                        .pctExcDupe(0D)
                        .pctExcUnpaired(0D)
                        .pctExcBaseQ(0D)
                        .pctExcOverlap(0D)
                        .pctExcCapped(0D)
                        .pctExcTotal(0D)
                        .build())
                .build();
    }
}
