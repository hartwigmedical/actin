package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakendType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxStructuralVariant;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

class DisruptionExtractor {

    private static final Logger LOGGER = LogManager.getLogger(DisruptionExtractor.class);

    @NotNull
    private final GeneFilter geneFilter;
    @NotNull
    private final EvidenceDatabase evidenceDatabase;

    public DisruptionExtractor(@NotNull final GeneFilter geneFilter, @NotNull final EvidenceDatabase evidenceDatabase) {
        this.geneFilter = geneFilter;
        this.evidenceDatabase = evidenceDatabase;
    }

    @NotNull
    public Set<Disruption> extractDisruptions(@NotNull LinxRecord linx, @NotNull Set<String> lostGenes) {
        Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
        for (LinxBreakend breakend : linx.breakends()) {
            String event = DriverEventFactory.disruptionEvent(breakend);
            if (geneFilter.include(breakend.gene())) {
                if (include(breakend, lostGenes)) {
                    disruptions.add(ImmutableDisruption.builder()
                            .from(GeneAlterationFactory.convertAlteration(breakend.gene(),
                                    evidenceDatabase.geneAlterationForBreakend(breakend)))
                            .isReportable(breakend.reported())
                            .event(event)
                            .driverLikelihood(DriverLikelihood.LOW)
                            .evidence(ActionableEvidenceFactory.create(evidenceDatabase.evidenceForBreakend(breakend)))
                            .type(determineDisruptionType(breakend.type()))
                            .junctionCopyNumber(ExtractionUtil.keep3Digits(breakend.junctionCopyNumber()))
                            .undisruptedCopyNumber(ExtractionUtil.keep3Digits(breakend.undisruptedCopyNumber()))
                            .regionType(determineRegionType(breakend.regionType()))
                            .codingContext(determineCodingContext(breakend.codingType()))
                            .clusterGroup(lookupClusterId(breakend, linx.structuralVariants()))
                            .build());
                }
            } else if (breakend.reported()) {
                LOGGER.warn("Filtered a reported breakend through gene filtering: '{}'", event);
            }
        }
        return disruptions;
    }

    private static boolean include(@NotNull LinxBreakend breakend, @NotNull Set<String> lostGenes) {
        return breakend.type() != LinxBreakendType.DEL || !lostGenes.contains(breakend.gene());
    }

    private static int lookupClusterId(@NotNull LinxBreakend breakend, @NotNull Set<LinxStructuralVariant> structuralVariants) {
        for (LinxStructuralVariant structuralVariant : structuralVariants) {
            if (structuralVariant.svId() == breakend.svId()) {
                return structuralVariant.clusterId();
            }
        }

        // TODO Switch to exception once linx assures all SVs are present. This will be implemented in v1.23
        LOGGER.warn("Could not resolve structural variant with id: {}", breakend.svId());
        return -1;
    }

    @NotNull
    @VisibleForTesting
    static DisruptionType determineDisruptionType(@NotNull LinxBreakendType type) {
        switch (type) {
            case BND: {
                return DisruptionType.BND;
            }
            case DEL: {
                return DisruptionType.DEL;
            }
            case DUP: {
                return DisruptionType.DUP;
            }
            case INF: {
                return DisruptionType.INF;
            }
            case INS: {
                return DisruptionType.INS;
            }
            case INV: {
                return DisruptionType.INV;
            }
            case SGL: {
                return DisruptionType.SGL;
            }
            default: {
                throw new IllegalStateException("Cannot determine disruption type for linx disruption type: " + type);
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static RegionType determineRegionType(@NotNull LinxRegionType regionType) {
        switch (regionType) {
            case UPSTREAM: {
                return RegionType.UPSTREAM;
            }
            case EXONIC: {
                return RegionType.EXONIC;
            }
            case INTRONIC: {
                return RegionType.INTRONIC;
            }
            case IG: {
                return RegionType.IG;
            }
            case DOWNSTREAM: {
                return RegionType.DOWNSTREAM;
            }
            default: {
                throw new IllegalStateException("Cannot determine region type for linx region type: " + regionType);
            }
        }
    }

    @NotNull
    @VisibleForTesting
    static CodingContext determineCodingContext(@NotNull LinxCodingType codingType) {
        switch (codingType) {
            case CODING: {
                return CodingContext.CODING;
            }
            case UTR_5P: {
                return CodingContext.UTR_5P;
            }
            case UTR_3P: {
                return CodingContext.UTR_3P;
            }
            case NON_CODING: {
                return CodingContext.NON_CODING;
            }
            case ENHANCER: {
                return CodingContext.ENHANCER;
            }
            default: {
                throw new IllegalStateException("Cannot determine coding context for linx coding type: " + codingType);
            }
        }
    }
}
