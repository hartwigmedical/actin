package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CopyNumberLookup {

    private CopyNumberLookup() {
    }

    @Nullable
    public static KnownCopyNumber findForCopyNumber(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull PurpleCopyNumber copyNumber) {
        if (copyNumber.interpretation().isGain()) {
            return findForAmplification(knownCopyNumbers, copyNumber.gene());
        } else if (copyNumber.interpretation().isLoss()) {
            return findForLoss(knownCopyNumbers, copyNumber.gene());
        }

        return null;
    }

    @Nullable
    public static KnownCopyNumber findForHomozygousDisruption(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull LinxHomozygousDisruption homozygousDisruption) {
        return findForLoss(knownCopyNumbers, homozygousDisruption.gene());
    }

    @Nullable
    private static KnownCopyNumber findForAmplification(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull String geneToFind) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.AMPLIFICATION && knownCopyNumber.gene().equals(geneToFind)) {
                return knownCopyNumber;
            }
        }
        return null;
    }

    @Nullable
    private static KnownCopyNumber findForLoss(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull String geneToFind) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.DELETION && knownCopyNumber.gene().equals(geneToFind)) {
                return knownCopyNumber;
            }
        }
        return null;
    }
}
