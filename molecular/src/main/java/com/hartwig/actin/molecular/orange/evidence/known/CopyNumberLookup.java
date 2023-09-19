package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CopyNumberLookup {

    private CopyNumberLookup() {
    }

    @Nullable
    public static KnownCopyNumber findForCopyNumber(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull PurpleGainLoss gainLoss) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            boolean geneMatches = knownCopyNumber.gene().equals(gainLoss.gene());
            boolean interpretationMatches = interpretationMatchesEvent(gainLoss.interpretation(), knownCopyNumber.event());
            if (geneMatches && interpretationMatches) {
                return knownCopyNumber;
            }
        }
        return null;
    }

    private static boolean interpretationMatchesEvent(@NotNull CopyNumberInterpretation interpretation, @NotNull GeneEvent event) {
        switch (interpretation) {
            case FULL_GAIN:
            case PARTIAL_GAIN: {
                return event == GeneEvent.AMPLIFICATION;
            }
            case FULL_LOSS:
            case PARTIAL_LOSS: {
                return event == GeneEvent.DELETION;
            }
            default: {
                return false;
            }
        }
    }

    @Nullable
    public static KnownCopyNumber findForHomozygousDisruption(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull LinxHomozygousDisruption homozygousDisruption) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.DELETION && knownCopyNumber.gene().equals(homozygousDisruption.gene())) {
                return knownCopyNumber;
            }
        }
        return null;
    }
}
