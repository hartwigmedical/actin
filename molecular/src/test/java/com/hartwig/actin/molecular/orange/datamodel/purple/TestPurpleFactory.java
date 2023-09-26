package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Collections;

import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.hmftools.datamodel.purple.Hotspot;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleAllelicDepth;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleCharacteristics;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleDriver;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleFit;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleQC;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleTranscriptImpact;
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleVariant;
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect;
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType;
import com.hartwig.hmftools.datamodel.purple.PurpleFittedPurityMethod;
import com.hartwig.hmftools.datamodel.purple.PurpleGenotypeStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleLikelihoodMethod;
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus;
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPurpleFactory {

    private TestPurpleFactory() {
    }

    @NotNull
    public static ImmutablePurpleFit.Builder fitBuilder() {
        return ImmutablePurpleFit.builder()
                .hasSufficientQuality(false)
                .containsTumorCells(false)
                .purity(0)
                .ploidy(0)
                .minPloidy(0D)
                .maxPloidy(0D)
                .minPurity(0D)
                .maxPurity(0D)
                .fittedPurityMethod(PurpleFittedPurityMethod.NORMAL)
                .qc(purpleQCBuilder().build());
    }

    @NotNull
    public static ImmutablePurpleCharacteristics.Builder characteristicsBuilder() {
        return ImmutablePurpleCharacteristics.builder()
                .microsatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN)
                .tumorMutationalBurdenPerMb(0D)
                .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.UNKNOWN)
                .tumorMutationalLoad(0)
                .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.UNKNOWN)
                .wholeGenomeDuplication(false)
                .microsatelliteIndelsPerMb(0D)
                .svTumorMutationalBurden(0);
    }

    @NotNull
    public static ImmutablePurpleDriver.Builder driverBuilder() {
        return ImmutablePurpleDriver.builder()
                .gene(Strings.EMPTY)
                .transcript(Strings.EMPTY)
                .driver(PurpleDriverType.MUTATION)
                .driverLikelihood(0D)
                .likelihoodMethod(PurpleLikelihoodMethod.NONE)
                .isCanonical(false);
    }

    @NotNull
    public static ImmutablePurpleVariant.Builder variantBuilder() {
        return ImmutablePurpleVariant.builder()
                .reported(true)
                .type(PurpleVariantType.SNP)
                .gene(Strings.EMPTY)
                .chromosome(Strings.EMPTY)
                .position(0)
                .ref(Strings.EMPTY)
                .alt(Strings.EMPTY)
                .adjustedCopyNumber(0D)
                .variantCopyNumber(0D)
                .hotspot(Hotspot.NON_HOTSPOT)
                .subclonalLikelihood(0D)
                .biallelic(false)
                .canonicalImpact(transcriptImpactBuilder().build())
                .worstCodingEffect(PurpleCodingEffect.NONE)
                .tumorDepth(ImmutablePurpleAllelicDepth.builder()
                        .totalReadCount(0)
                        .alleleReadCount(0)
                        .build())
                .adjustedVAF(0D)
                .minorAlleleCopyNumber(0D)
                .genotypeStatus(PurpleGenotypeStatus.UNKNOWN)
                .repeatCount(0);
    }

    @NotNull
    public static ImmutablePurpleTranscriptImpact.Builder transcriptImpactBuilder() {
        return ImmutablePurpleTranscriptImpact.builder()
                .transcript(Strings.EMPTY)
                .hgvsCodingImpact(Strings.EMPTY)
                .hgvsProteinImpact(Strings.EMPTY)
                .spliceRegion(false)
                .codingEffect(PurpleCodingEffect.UNDEFINED);
    }

    @NotNull
    public static ImmutablePurpleGainLoss.Builder gainLossBuilder() {
        return ImmutablePurpleGainLoss.builder()
                .gene(Strings.EMPTY)
                .interpretation(CopyNumberInterpretation.FULL_LOSS)
                .minCopies(0)
                .maxCopies(0)
                .chromosome(Strings.EMPTY)
                .chromosomeBand(Strings.EMPTY)
                .transcript(Strings.EMPTY)
                .isCanonical(true);
    }

    @NotNull
    public static ImmutablePurpleQC.Builder purpleQCBuilder() {
        return ImmutablePurpleQC.builder()
                .status(Collections.emptySet())
                .amberMeanDepth(0)
                .contamination(0D)
                .unsupportedCopyNumberSegments(0)
                .deletedGenes(0);
    }
}
