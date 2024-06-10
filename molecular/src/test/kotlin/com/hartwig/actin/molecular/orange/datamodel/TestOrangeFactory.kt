package com.hartwig.actin.molecular.orange.datamodel

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.molecular.orange.datamodel.cuppa.TestCuppaFactory
import com.hartwig.actin.molecular.orange.datamodel.lilac.TestLilacFactory
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.hmftools.datamodel.chord.ChordRecord
import com.hartwig.hmftools.datamodel.chord.ChordStatus
import com.hartwig.hmftools.datamodel.chord.ImmutableChordRecord
import com.hartwig.hmftools.datamodel.cuppa.CuppaData
import com.hartwig.hmftools.datamodel.cuppa.ImmutableCuppaData
import com.hartwig.hmftools.datamodel.flagstat.ImmutableFlagstat
import com.hartwig.hmftools.datamodel.hla.ImmutableLilacRecord
import com.hartwig.hmftools.datamodel.hla.LilacRecord
import com.hartwig.hmftools.datamodel.immuno.ImmuneEscapeRecord
import com.hartwig.hmftools.datamodel.immuno.ImmutableImmuneEscapeRecord
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import com.hartwig.hmftools.datamodel.linx.LinxRecord
import com.hartwig.hmftools.datamodel.metrics.ImmutableWGSMetrics
import com.hartwig.hmftools.datamodel.orange.ExperimentType
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangePlots
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeRecord
import com.hartwig.hmftools.datamodel.orange.ImmutableOrangeSample
import com.hartwig.hmftools.datamodel.orange.OrangePlots
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.hmftools.datamodel.orange.OrangeSample
import com.hartwig.hmftools.datamodel.peach.ImmutablePeachGenotype
import com.hartwig.hmftools.datamodel.peach.PeachGenotype
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleFit
import com.hartwig.hmftools.datamodel.purple.PurpleLikelihoodMethod
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleQCStatus
import com.hartwig.hmftools.datamodel.purple.PurpleRecord
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterData
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType
import org.apache.logging.log4j.util.Strings
import java.time.LocalDate

object TestOrangeFactory {

    fun createMinimalTestOrangeRecord(): OrangeRecord {
        return ImmutableOrangeRecord.builder()
            .sampleId(TestPatientFactory.TEST_SAMPLE)
            .samplingDate(LocalDate.of(2021, 5, 6))
            .experimentType(ExperimentType.WHOLE_GENOME)
            .refGenomeVersion(OrangeRefGenomeVersion.V37)
            .purple(createMinimalTestPurpleRecord())
            .linx(ImmutableLinxRecord.builder().build())
            .lilac(createMinimalTestLilacRecord())
            .tumorSample(createOrangeTumorSample())
            .plots(createOrangePlots())
            .immuneEscape(createImmuneEscapeRecord())
            .build()
    }

    private fun createMinimalTestPurpleRecord(): PurpleRecord {
        return ImmutablePurpleRecord.builder()
            .fit(TestPurpleFactory.fitBuilder()
                .qc(TestPurpleFactory.purpleQCBuilder().status(setOf<PurpleQCStatus?>(PurpleQCStatus.FAIL_NO_TUMOR)).build())
                .build())
            .characteristics(TestPurpleFactory.characteristicsBuilder().build())
            .build()
    }

    private fun createMinimalTestLilacRecord(): LilacRecord {
        return ImmutableLilacRecord.builder().qc(Strings.EMPTY).build()
    }

    fun createProperTestOrangeRecord(): OrangeRecord {
        return ImmutableOrangeRecord.builder()
            .from(createMinimalTestOrangeRecord())
            .purple(createTestPurpleRecord())
            .linx(createTestLinxRecord())
            .addPeach(createTestPeachGenotype())
            .cuppa(createTestCuppaRecord())
            .virusInterpreter(createTestVirusInterpreterRecord())
            .lilac(createTestLilacRecord())
            .chord(createTestChordRecord())
            .immuneEscape(createImmuneEscapeRecord())
            .build()
    }

    private fun createTestPurpleRecord(): PurpleRecord {
        return ImmutablePurpleRecord.builder()
            .from(createMinimalTestPurpleRecord())
            .fit(createTestPurpleFit())
            .characteristics(createTestPurpleCharacteristics())
            .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                .gene("BRAF")
                .type(PurpleDriverType.MUTATION)
                .driverLikelihood(1.0)
                .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                .isCanonical(false)
                .build())
            .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                .gene("MYC")
                .type(PurpleDriverType.AMP)
                .driverLikelihood(1.0)
                .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                .isCanonical(true)
                .build())
            .addSomaticDrivers(TestPurpleFactory.driverBuilder()
                .gene("PTEN")
                .type(PurpleDriverType.DEL)
                .driverLikelihood(1.0)
                .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                .isCanonical(true)
                .build())
            .addAllSomaticVariants(TestPurpleFactory.variantBuilder()
                .gene("BRAF")
                .adjustedCopyNumber(6.0)
                .variantCopyNumber(4.1)
                .hotspot(HotspotType.HOTSPOT)
                .subclonalLikelihood(0.02)
                .biallelic(false)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                    .hgvsCodingImpact("c.something")
                    .hgvsProteinImpact("p.Val600Glu")
                    .inSpliceRegion(false)
                    .addEffects(PurpleVariantEffect.MISSENSE)
                    .codingEffect(PurpleCodingEffect.MISSENSE)
                    .reported(true)
                    .build())
                .build())
            .addAllSomaticGainsLosses(TestPurpleFactory.gainLossBuilder()
                .gene("MYC")
                .interpretation(CopyNumberInterpretation.FULL_GAIN)
                .minCopies(38.0)
                .maxCopies(40.0)
                .build())
            .addAllSomaticGainsLosses(TestPurpleFactory.gainLossBuilder()
                .gene("PTEN")
                .interpretation(CopyNumberInterpretation.FULL_LOSS)
                .minCopies(0.0)
                .maxCopies(0.0)
                .build())
            .addAllSomaticGeneCopyNumbers(TestPurpleFactory.geneCopyNumberBuilder()
                .gene("AR")
                .minCopyNumber(3.2)
                .minMinorAlleleCopyNumber(0.0)
                .build())
            .addAllSomaticGeneCopyNumbers(TestPurpleFactory.geneCopyNumberBuilder()
                .gene("PTEN")
                .minCopyNumber(0.1)
                .minMinorAlleleCopyNumber(0.0)
                .build())
            .addAllSomaticGeneCopyNumbers(TestPurpleFactory.geneCopyNumberBuilder()
                .gene("MYC")
                .minCopyNumber(38.0)
                .minMinorAlleleCopyNumber(2.0)
                .build())
            .build()
    }

    private fun createTestPurpleFit(): PurpleFit {
        return TestPurpleFactory.fitBuilder()
            .purity(0.98)
            .ploidy(3.1)
            .qc(TestPurpleFactory.purpleQCBuilder().addStatus(PurpleQCStatus.PASS).build())
            .build()
    }

    private fun createTestPurpleCharacteristics(): PurpleCharacteristics {
        return TestPurpleFactory.characteristicsBuilder()
            .microsatelliteStatus(PurpleMicrosatelliteStatus.MSS)
            .tumorMutationalBurdenPerMb(13.0)
            .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.HIGH)
            .tumorMutationalLoad(189)
            .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.HIGH)
            .build()
    }

    private fun createTestLinxRecord(): LinxRecord {
        return ImmutableLinxRecord.builder()
            .addAllSomaticStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(1).build())
            .addSomaticHomozygousDisruptions(TestLinxFactory.homozygousDisruptionBuilder().gene("TP53").build())
            .addAllSomaticBreakends(TestLinxFactory.breakendBuilder()
                .reported(true)
                .svId(1)
                .gene("RB1")
                .type(LinxBreakendType.DEL)
                .junctionCopyNumber(0.8)
                .undisruptedCopyNumber(2.1)
                .build())
            .addAllSomaticBreakends(TestLinxFactory.breakendBuilder()
                .reported(true)
                .svId(1)
                .gene("PTEN")
                .type(LinxBreakendType.DEL)
                .junctionCopyNumber(1.0)
                .undisruptedCopyNumber(1.0)
                .build())
            .addAllSomaticFusions(TestLinxFactory.fusionBuilder()
                .reported(true)
                .reportedType(LinxFusionType.KNOWN_PAIR)
                .geneStart("EML4")
                .fusedExonUp(2)
                .geneEnd("ALK")
                .fusedExonDown(4)
                .driverLikelihood(FusionLikelihoodType.HIGH)
                .build())
            .build()
    }

    private fun createTestPeachGenotype(): PeachGenotype {
        return ImmutablePeachGenotype.builder()
            .gene("DPYD")
            .haplotype("1* HOM")
            .function("Normal function")
            .linkedDrugs(Strings.EMPTY)
            .urlPrescriptionInfo(Strings.EMPTY)
            .panelVersion(Strings.EMPTY)
            .repoVersion(Strings.EMPTY)
            .build()
    }

    private fun createTestCuppaRecord(): CuppaData {
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
            .build()
    }

    private fun createTestVirusInterpreterRecord(): VirusInterpreterData {
        return ImmutableVirusInterpreterData.builder()
            .addAllViruses(TestVirusInterpreterFactory.builder()
                .reported(true)
                .name("Human papillomavirus type 16")
                .qcStatus(VirusBreakendQCStatus.NO_ABNORMALITIES)
                .interpretation(VirusInterpretation.HPV)
                .integrations(3)
                .driverLikelihood(VirusLikelihoodType.HIGH)
                .build())
            .build()
    }

    private fun createTestLilacRecord(): LilacRecord {
        return ImmutableLilacRecord.builder()
            .qc("PASS")
            .addAlleles(TestLilacFactory.builder()
                .allele("A*01:01")
                .tumorCopyNumber(1.2)
                .somaticSynonymous(0.0)
                .refFragments(0)
                .tumorFragments(0)
                .rnaFragments(0)
                .build())
            .build()
    }

    private fun createTestChordRecord(): ChordRecord {
        return ImmutableChordRecord.builder()
            .hrStatus(ChordStatus.HR_PROFICIENT)
            .brca1Value(0.0)
            .brca2Value(0.0)
            .hrdValue(0.45)
            .hrdType(Strings.EMPTY)
            .build()
    }

    private fun createOrangePlots(): OrangePlots {
        return ImmutableOrangePlots.builder()
            .sageTumorBQRPlot(Strings.EMPTY)
            .purpleInputPlot(Strings.EMPTY)
            .purpleFinalCircosPlot(Strings.EMPTY)
            .purpleClonalityPlot(Strings.EMPTY)
            .purpleCopyNumberPlot(Strings.EMPTY)
            .purpleVariantCopyNumberPlot(Strings.EMPTY)
            .purplePurityRangePlot(Strings.EMPTY)
            .purpleKataegisPlot(Strings.EMPTY)
            .build()
    }

    private fun createOrangeTumorSample(): OrangeSample {
        return ImmutableOrangeSample.builder()
            .flagstat(ImmutableFlagstat.builder()
                .uniqueReadCount(0L)
                .secondaryCount(0L)
                .supplementaryCount(0L)
                .mappedProportion(0.0)
                .build())
            .metrics(ImmutableWGSMetrics.builder()
                .meanCoverage(0.0)
                .sdCoverage(0.0)
                .medianCoverage(0)
                .madCoverage(0)
                .pctExcMapQ(0.0)
                .pctExcDupe(0.0)
                .pctExcUnpaired(0.0)
                .pctExcBaseQ(0.0)
                .pctExcOverlap(0.0)
                .pctExcCapped(0.0)
                .pctExcTotal(0.0)
                .build())
            .build()
    }

    private fun createImmuneEscapeRecord(): ImmuneEscapeRecord {
        return ImmutableImmuneEscapeRecord.builder()
            .hasHlaEscape(false)
            .hasAntigenPresentationPathwayEscape(false)
            .hasIFNGammaPathwayEscape(false)
            .hasPDL1OverexpressionEscape(false)
            .hasCD58InactivationEscape(false)
            .hasEpigeneticSETDB1Escape(false)
            .build()
    }
}
