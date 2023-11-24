package com.hartwig.actin.molecular.orange.datamodel.linx

import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType
import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType
import com.hartwig.hmftools.datamodel.linx.FusionPhasedType
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxBreakend
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxDriver
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxFusion
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxSvAnnotation
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType
import com.hartwig.hmftools.datamodel.linx.LinxDriverType
import com.hartwig.hmftools.datamodel.linx.LinxFusionType
import org.apache.logging.log4j.util.Strings

object TestLinxFactory {

    fun structuralVariantBuilder(): ImmutableLinxSvAnnotation.Builder {
        return ImmutableLinxSvAnnotation.builder()
            .svId(0)
            .clusterId(0)
            .vcfId(Strings.EMPTY)
            .clusterReason(Strings.EMPTY)
            .fragileSiteStart(false)
            .fragileSiteEnd(false)
            .isFoldback(false)
            .lineTypeStart(Strings.EMPTY)
            .lineTypeEnd(Strings.EMPTY)
            .junctionCopyNumberMin(0.0)
            .junctionCopyNumberMax(0.0)
            .geneStart(Strings.EMPTY)
            .geneEnd(Strings.EMPTY)
            .localTopologyStart(Strings.EMPTY)
            .localTopologyEnd(Strings.EMPTY)
            .localTopologyIdStart(0)
            .localTopologyIdEnd(0)
            .localTICountStart(0)
            .localTICountEnd(0)
    }

    fun homozygousDisruptionBuilder(): ImmutableLinxHomozygousDisruption.Builder {
        return ImmutableLinxHomozygousDisruption.builder()
            .gene(Strings.EMPTY)
            .chromosome(Strings.EMPTY)
            .chromosomeBand(Strings.EMPTY)
            .transcript(Strings.EMPTY)
            .isCanonical(false)
    }

    fun breakendBuilder(): ImmutableLinxBreakend.Builder {
        return ImmutableLinxBreakend.builder()
            .reported(true)
            .svId(0)
            .gene(Strings.EMPTY)
            .type(LinxBreakendType.BND)
            .junctionCopyNumber(0.0)
            .undisruptedCopyNumber(0.0)
            .regionType(TranscriptRegionType.INTRONIC)
            .codingType(TranscriptCodingType.NON_CODING)
            .transcript(Strings.EMPTY)
            .isCanonical(false)
            .geneOrientation(Strings.EMPTY)
            .disruptive(false)
            .nextSpliceExonRank(0)
            .chromosome(Strings.EMPTY)
            .orientation(0)
            .chromosomeBand(Strings.EMPTY)
            .exonUp(0)
            .exonDown(0)
            .id(0)
    }

    fun driverBuilder(): ImmutableLinxDriver.Builder {
        return ImmutableLinxDriver.builder()
            .gene("")
            .type(LinxDriverType.UNCLEAR)
    }

    fun fusionBuilder(): ImmutableLinxFusion.Builder {
        return ImmutableLinxFusion.builder()
            .reported(true)
            .reportedType(LinxFusionType.NONE)
            .geneStart(Strings.EMPTY)
            .geneTranscriptStart(Strings.EMPTY)
            .fusedExonUp(0)
            .geneEnd(Strings.EMPTY)
            .geneTranscriptEnd(Strings.EMPTY)
            .fusedExonDown(0)
            .driverLikelihood(FusionLikelihoodType.LOW)
            .phased(FusionPhasedType.INFRAME)
            .chainLinks(0)
            .chainTerminated(false)
            .domainsKept(Strings.EMPTY)
            .domainsLost(Strings.EMPTY)
            .geneContextStart(Strings.EMPTY)
            .geneContextEnd(Strings.EMPTY)
            .junctionCopyNumber(0.0)
    }
}
