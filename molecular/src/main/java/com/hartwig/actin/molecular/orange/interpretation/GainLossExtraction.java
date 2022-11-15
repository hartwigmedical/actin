package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.orange.datamodel.purple.GainLossInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleGainLoss;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;

import org.jetbrains.annotations.NotNull;

final class GainLossExtraction {

    private GainLossExtraction() {
    }

    @NotNull
    public static Set<Amplification> extractAmplifications(@NotNull PurpleRecord purple) {
        Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleGainLoss gainLoss : purple.gainsLosses()) {
            if (gainLoss.interpretation() == GainLossInterpretation.PARTIAL_GAIN
                    || gainLoss.interpretation() == GainLossInterpretation.FULL_GAIN) {
                boolean isPartial = gainLoss.interpretation() == GainLossInterpretation.PARTIAL_GAIN;
                amplifications.add(ImmutableAmplification.builder()
                        .from(ExtractionUtil.createBaseGeneAlteration(gainLoss.gene()))
                        .isReportable(true)
                        .event(DriverEventFactory.gainLossEvent(gainLoss))
                        .driverLikelihood(isPartial ? DriverLikelihood.MEDIUM : DriverLikelihood.HIGH)
                        .evidence(ExtractionUtil.createEmptyEvidence())
                        .minCopies(gainLoss.minCopies())
                        .maxCopies(0)
                        .isPartial(isPartial)
                        .build());
            }
        }
        return amplifications;
    }

    @NotNull
    public static Set<Loss> extractLosses(@NotNull PurpleRecord purple) {
        Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleGainLoss gainLoss : purple.gainsLosses()) {
            if (gainLoss.interpretation() == GainLossInterpretation.PARTIAL_LOSS
                    || gainLoss.interpretation() == GainLossInterpretation.FULL_LOSS) {
                losses.add(ImmutableLoss.builder()
                        .from(ExtractionUtil.createBaseGeneAlteration(gainLoss.gene()))
                        .isReportable(true)
                        .event(DriverEventFactory.gainLossEvent(gainLoss))
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(ExtractionUtil.createEmptyEvidence())
                        .minCopies(gainLoss.minCopies())
                        .maxCopies(0)
                        .isPartial(gainLoss.interpretation() == GainLossInterpretation.PARTIAL_LOSS)
                        .build());
            }
        }
        return losses;
    }
}
