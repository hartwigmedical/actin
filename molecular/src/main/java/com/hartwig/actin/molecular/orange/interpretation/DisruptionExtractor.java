package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase;
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator;

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
    public Set<HomozygousDisruption> extractHomozygousDisruptions(@NotNull LinxRecord linx) {
        Set<HomozygousDisruption> homozygousDisruptions = Sets.newTreeSet(new HomozygousDisruptionComparator());
        for (LinxHomozygousDisruption homozygousDisruption : linx.homozygousDisruptions()) {
            if (geneFilter.include(homozygousDisruption.gene())) {
                homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                        .from(GeneAlterationFactory.convertAlteration(homozygousDisruption.gene(),
                                evidenceDatabase.lookupGeneAlteration(homozygousDisruption)))
                        .isReportable(true)
                        .event(DriverEventFactory.homozygousDisruptionEvent(homozygousDisruption))
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(ActionableEvidenceFactory.create(evidenceDatabase.matchToActionableEvidence(homozygousDisruption)))
                        .build());
            } else {
                LOGGER.warn("Filtered a reported homozygous disruption on gene {}", homozygousDisruption.gene());
            }
        }
        return homozygousDisruptions;
    }

    @NotNull
    public Set<Disruption> extractDisruptions(@NotNull LinxRecord linx, @NotNull Set<Loss> losses) {
        Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
        for (LinxDisruption disruption : linx.disruptions()) {
            if (disruption.clusterId() == null) {
                throw new IllegalStateException("Cannot convert a disruption with null clusterId: " + disruption);
            }

            if (geneFilter.include(disruption.gene())) {
                // TODO: Linx should already filter or flag disruptions that are lost.
                // TODO: Populate region type and coding context
                if (include(disruption, losses)) {
                    disruptions.add(ImmutableDisruption.builder()
                            .from(GeneAlterationFactory.convertAlteration(disruption.gene(),
                                    evidenceDatabase.lookupGeneAlteration(disruption)))
                            .isReportable(disruption.reported())
                            .event(DriverEventFactory.disruptionEvent(disruption))
                            .driverLikelihood(DriverLikelihood.LOW)
                            .evidence(ActionableEvidenceFactory.create(evidenceDatabase.matchToActionableEvidence(disruption)))
                            .type(disruption.type())
                            .junctionCopyNumber(ExtractionUtil.keep3Digits(disruption.junctionCopyNumber()))
                            .undisruptedCopyNumber(ExtractionUtil.keep3Digits(disruption.undisruptedCopyNumber()))
                            .regionType(RegionType.INTRONIC)
                            .codingContext(CodingContext.NON_CODING)
                            .clusterGroup(disruption.clusterId())
                            .build());
                }
            } else if (disruption.reported()) {
                LOGGER.warn("Filtered a reported disruption on gene {}", disruption.gene());
            }
        }
        return disruptions;
    }

    private static boolean include(@NotNull LinxDisruption disruption, @NotNull Set<Loss> losses) {
        return !disruption.type().equalsIgnoreCase("del") || !isLost(losses, disruption.gene());
    }

    private static boolean isLost(@NotNull Set<Loss> losses, @NotNull String gene) {
        for (Loss loss : losses) {
            if (loss.gene().equals(gene)) {
                return true;
            }
        }
        return false;
    }
}
