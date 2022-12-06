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
    public static KnownCopyNumber findForAmplification(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull PurpleCopyNumber amp) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.AMPLIFICATION && knownCopyNumber.gene().equals(amp.gene())) {
                return knownCopyNumber;
            }
        }
        return null;
    }

    @Nullable
    public static KnownCopyNumber findForLoss(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull PurpleCopyNumber loss) {
        return findForDeletion(knownCopyNumbers, loss.gene());
    }

    @Nullable
    public static KnownCopyNumber findForHomozygousDisruption(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull LinxHomozygousDisruption homozygousDisruption) {
        return findForDeletion(knownCopyNumbers, homozygousDisruption.gene());
    }

    @Nullable
    private static KnownCopyNumber findForDeletion(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers, @NotNull String geneToFind) {
        for (KnownCopyNumber knownCopyNumber : knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.DELETION && knownCopyNumber.gene().equals(geneToFind)) {
                return knownCopyNumber;
            }
        }
        return null;
    }
}
