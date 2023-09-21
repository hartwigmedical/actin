package com.hartwig.actin.molecular.orange.datamodel.linx;

import com.hartwig.hmftools.datamodel.linx.FusionLikelihoodType;
import com.hartwig.hmftools.datamodel.linx.FusionPhasedType;
import com.hartwig.hmftools.datamodel.linx.ImmutableHomozygousDisruption;
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxSvAnnotation;
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxBreakend;
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxFusion;
import com.hartwig.hmftools.datamodel.linx.LinxFusionType;
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType;
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType;
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLinxFactory {

    private TestLinxFactory() {
    }

    @NotNull
    public static ImmutableLinxSvAnnotation.Builder structuralVariantBuilder() {
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
                .junctionCopyNumberMin(0D)
                .junctionCopyNumberMax(0D)
                .geneStart(Strings.EMPTY)
                .geneEnd(Strings.EMPTY)
                .localTopologyStart(Strings.EMPTY)
                .localTopologyEnd(Strings.EMPTY)
                .localTopologyIdStart(0)
                .localTopologyIdEnd(0)
                .localTICountStart(0)
                .localTICountEnd(0)
                ;
    }

    @NotNull
    public static ImmutableHomozygousDisruption.Builder homozygousDisruptionBuilder() {
        return ImmutableHomozygousDisruption.builder()
                .gene(Strings.EMPTY)
                .chromosome(Strings.EMPTY)
                .chromosomeBand(Strings.EMPTY)
                .transcript(Strings.EMPTY)
                .isCanonical(false);
    }

    @NotNull
    public static ImmutableLinxBreakend.Builder breakendBuilder() {
        return ImmutableLinxBreakend.builder()
                .reportedDisruption(true)
                .svId(0)
                .gene(Strings.EMPTY)
                .type(LinxBreakendType.BND)
                .junctionCopyNumber(0D)
                .undisruptedCopyNumber(0D)
                .regionType(TranscriptRegionType.INTRONIC)
                .codingType(TranscriptCodingType.NON_CODING)
                .transcriptId(Strings.EMPTY)
                .canonical(false)
                .geneOrientation(Strings.EMPTY)
                .disruptive(false)
                .nextSpliceExonRank(0)
                .chromosome(Strings.EMPTY)
                .orientation(0)
                .strand(0)
                .chrBand(Strings.EMPTY)
                .exonUp(0)
                .exonDown(0)
                .id(0);
    }

    @NotNull
    public static ImmutableLinxFusion.Builder fusionBuilder() {
        return ImmutableLinxFusion.builder()
                .reported(true)
                .reportedType(LinxFusionType.NONE)
                .geneStart(Strings.EMPTY)
                .geneTranscriptStart(Strings.EMPTY)
                .fusedExonUp(0)
                .geneEnd(Strings.EMPTY)
                .geneTranscriptEnd(Strings.EMPTY)
                .fusedExonDown(0)
                .likelihood(FusionLikelihoodType.LOW)
                .name(Strings.EMPTY)
                .phased(FusionPhasedType.INFRAME)
                .chainLinks(0)
                .chainTerminated(false)
                .domainsKept(Strings.EMPTY)
                .domainsLost(Strings.EMPTY)
                .geneContextStart(Strings.EMPTY)
                .geneContextEnd(Strings.EMPTY)
                .junctionCopyNumber(0D);
    }
}
