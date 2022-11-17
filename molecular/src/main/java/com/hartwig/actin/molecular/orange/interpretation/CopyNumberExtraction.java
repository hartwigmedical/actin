package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableAmplification;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableLoss;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.orange.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.sort.driver.CopyNumberComparator;

import org.jetbrains.annotations.NotNull;

final class CopyNumberExtraction {

    private CopyNumberExtraction() {
    }

    @NotNull
    public static Set<Amplification> extractAmplifications(@NotNull PurpleRecord purple) {
        Set<Amplification> amplifications = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            if (copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_GAIN
                    || copyNumber.interpretation() == CopyNumberInterpretation.FULL_GAIN) {
                boolean isPartial = copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_GAIN;
                amplifications.add(ImmutableAmplification.builder()
                        .from(ExtractionUtil.createBaseGeneAlteration(copyNumber.gene()))
                        .isReportable(true)
                        .event(DriverEventFactory.copyNumberEvent(copyNumber))
                        .driverLikelihood(isPartial ? DriverLikelihood.MEDIUM : DriverLikelihood.HIGH)
                        .evidence(ExtractionUtil.createEmptyEvidence())
                        .minCopies(copyNumber.minCopies())
                        .maxCopies(copyNumber.maxCopies())
                        .isPartial(isPartial)
                        .build());
            }
        }
        return amplifications;
    }

    @NotNull
    public static Set<Loss> extractLosses(@NotNull PurpleRecord purple) {
        Set<Loss> losses = Sets.newTreeSet(new CopyNumberComparator());
        for (PurpleCopyNumber copyNumber : purple.copyNumbers()) {
            if (copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_LOSS
                    || copyNumber.interpretation() == CopyNumberInterpretation.FULL_LOSS) {
                losses.add(ImmutableLoss.builder()
                        .from(ExtractionUtil.createBaseGeneAlteration(copyNumber.gene()))
                        .isReportable(true)
                        .event(DriverEventFactory.copyNumberEvent(copyNumber))
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .evidence(ExtractionUtil.createEmptyEvidence())
                        .minCopies(copyNumber.minCopies())
                        .maxCopies(copyNumber.maxCopies())
                        .isPartial(copyNumber.interpretation() == CopyNumberInterpretation.PARTIAL_LOSS)
                        .build());
            }
        }
        return losses;
    }
}
