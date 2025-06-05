package com.hartwig.actin.molecular.orange.datamodel.purple

import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.HotspotType
import com.hartwig.hmftools.datamodel.purple.ImmutableChromosomalRearrangements
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleAllelicDepth
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleCharacteristics
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleDriver
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleFit
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleGainDeletion
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleGeneCopyNumber
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleQC
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleTranscriptImpact
import com.hartwig.hmftools.datamodel.purple.ImmutablePurpleVariant
import com.hartwig.hmftools.datamodel.purple.ImmutableTumorStats
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType
import com.hartwig.hmftools.datamodel.purple.PurpleFittedPurityMethod
import com.hartwig.hmftools.datamodel.purple.PurpleGenotypeStatus
import com.hartwig.hmftools.datamodel.purple.PurpleLikelihoodMethod
import com.hartwig.hmftools.datamodel.purple.PurpleMicrosatelliteStatus
import com.hartwig.hmftools.datamodel.purple.PurpleTumorMutationalStatus
import com.hartwig.hmftools.datamodel.purple.PurpleVariantType

object TestPurpleFactory {

    fun fitBuilder(): ImmutablePurpleFit.Builder {
        return ImmutablePurpleFit.builder()
            .purity(0.0)
            .ploidy(0.0)
            .minPloidy(0.0)
            .maxPloidy(0.0)
            .minPurity(0.0)
            .maxPurity(0.0)
            .fittedPurityMethod(PurpleFittedPurityMethod.NORMAL)
            .qc(purpleQCBuilder().build())
    }

    fun tumorStatsBuilder(): ImmutableTumorStats.Builder {
        return ImmutableTumorStats.builder()
            .maxDiploidProportion(0.0)
            .hotspotMutationCount(0)
            .hotspotStructuralVariantCount(0)
            .smallVariantAlleleReadCount(0)
            .structuralVariantTumorFragmentCount(0)
            .bafCount(0)
    }

    fun characteristicsBuilder(): ImmutablePurpleCharacteristics.Builder {
        return ImmutablePurpleCharacteristics.builder()
            .microsatelliteIndelsPerMb(0.0)
            .microsatelliteStatus(PurpleMicrosatelliteStatus.UNKNOWN)
            .tumorMutationalBurdenPerMb(0.0)
            .tumorMutationalBurdenStatus(PurpleTumorMutationalStatus.UNKNOWN)
            .tumorMutationalLoad(0)
            .tumorMutationalLoadStatus(PurpleTumorMutationalStatus.UNKNOWN)
            .wholeGenomeDuplication(false)
            .svTumorMutationalBurden(0)
    }

    fun chromosomalRearrangementsBuilder(): ImmutableChromosomalRearrangements.Builder {
        return ImmutableChromosomalRearrangements.builder()
            .hasTrisomy1q(false)
            .hasCodeletion1p19q(false)
    }

    fun driverBuilder(): ImmutablePurpleDriver.Builder {
        return ImmutablePurpleDriver.builder()
            .gene("")
            .transcript("")
            .type(PurpleDriverType.MUTATION)
            .driverLikelihood(0.0)
            .likelihoodMethod(PurpleLikelihoodMethod.NONE)
            .isCanonical(false)
    }

    fun variantBuilder(): ImmutablePurpleVariant.Builder {
        return ImmutablePurpleVariant.builder()
            .type(PurpleVariantType.SNP)
            .gene("")
            .chromosome("")
            .position(0)
            .ref("")
            .alt("")
            .adjustedCopyNumber(0.0)
            .variantCopyNumber(0.0)
            .hotspot(HotspotType.NON_HOTSPOT)
            .subclonalLikelihood(0.0)
            .biallelic(false)
            .canonicalImpact(transcriptImpactBuilder().reported(true).build())
            .worstCodingEffect(PurpleCodingEffect.NONE)
            .tumorDepth(ImmutablePurpleAllelicDepth.builder().totalReadCount(0).alleleReadCount(0).build())
            .adjustedVAF(0.0)
            .minorAlleleCopyNumber(0.0)
            .genotypeStatus(PurpleGenotypeStatus.UNKNOWN)
            .repeatCount(0)
    }

    fun transcriptImpactBuilder(): ImmutablePurpleTranscriptImpact.Builder {
        return ImmutablePurpleTranscriptImpact.builder()
            .transcript("")
            .hgvsCodingImpact("")
            .hgvsProteinImpact("")
            .inSpliceRegion(false)
            .codingEffect(PurpleCodingEffect.UNDEFINED)
            .reported(false)
    }

    fun gainDelBuilder(): ImmutablePurpleGainDeletion.Builder {
        return ImmutablePurpleGainDeletion.builder()
            .gene("")
            .interpretation(CopyNumberInterpretation.FULL_DEL)
            .minCopies(0.0)
            .maxCopies(0.0)
            .chromosome("")
            .chromosomeBand("")
            .transcript("")
            .isCanonical(true)
    }

    fun geneCopyNumberBuilder(): ImmutablePurpleGeneCopyNumber.Builder {
        return ImmutablePurpleGeneCopyNumber.builder()
            .gene("")
            .chromosome("")
            .chromosomeBand("")
            .transcript("")
            .isCanonical(false)
            .minCopyNumber(0.0)
            .maxCopyNumber(0.0)
            .minMinorAlleleCopyNumber(0.0)
    }

    fun purpleQCBuilder(): ImmutablePurpleQC.Builder {
        return ImmutablePurpleQC.builder()
            .status(emptySet())
            .addAllGermlineAberrations(emptySet())
            .amberMeanDepth(0)
            .contamination(0.0)
            .totalCopyNumberSegments(0)
            .unsupportedCopyNumberSegments(0)
            .deletedGenes(0)
    }
}
