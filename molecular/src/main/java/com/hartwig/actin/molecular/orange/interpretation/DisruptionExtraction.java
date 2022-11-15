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
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.sort.driver.DisruptionComparator;
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator;

import org.jetbrains.annotations.NotNull;

final class DisruptionExtraction {

    private DisruptionExtraction() {
    }

    @NotNull
    public static Set<HomozygousDisruption> extractHomozygousDisruptions(@NotNull LinxRecord linx) {
        Set<HomozygousDisruption> homozygousDisruptions = Sets.newTreeSet(new HomozygousDisruptionComparator());
        for (String homozygous : linx.homozygousDisruptedGenes()) {
            homozygousDisruptions.add(ImmutableHomozygousDisruption.builder()
                    .from(ExtractionUtil.createBaseGeneAlteration(homozygous))
                    .isReportable(true)
                    .event(homozygous + " hom disruption")
                    .driverLikelihood(DriverLikelihood.HIGH)
                    .evidence(ExtractionUtil.createEmptyEvidence())
                    .build());
        }
        return homozygousDisruptions;
    }

    @NotNull
    public static Set<Disruption> extractDisruptions(@NotNull LinxRecord linx, @NotNull Set<Loss> losses) {
        Set<Disruption> disruptions = Sets.newTreeSet(new DisruptionComparator());
        for (LinxDisruption disruption : linx.disruptions()) {
            // TODO: Linx should already filter or flag disruptions that are lost.
            // TODO: Populate region type and coding context
            if (include(disruption, losses)) {
                disruptions.add(ImmutableDisruption.builder()
                        .from(ExtractionUtil.createBaseGeneAlteration(disruption.gene()))
                        .isReportable(true)
                        .event(DriverEventFactory.disruptionEvent(disruption))
                        .driverLikelihood(DriverLikelihood.LOW)
                        .evidence(ExtractionUtil.createEmptyEvidence())
                        .type(disruption.type())
                        .junctionCopyNumber(ExtractionUtil.keep3Digits(disruption.junctionCopyNumber()))
                        .undisruptedCopyNumber(ExtractionUtil.keep3Digits(disruption.undisruptedCopyNumber()))
                        .regionType(RegionType.INTRONIC)
                        .codingContext(CodingContext.NON_CODING)
                        .clusterGroup(disruption.clusterId())
                        .build());
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
