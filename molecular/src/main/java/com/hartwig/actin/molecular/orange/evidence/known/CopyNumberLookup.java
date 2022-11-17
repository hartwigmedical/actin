package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CopyNumberLookup {

    private CopyNumberLookup() {
    }

    @Nullable
    public static KnownCopyNumber findForCopyNumber(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull PurpleCopyNumber copyNumber) {
        return null;
    }

    @Nullable
    public static KnownCopyNumber findForHomozygousDisruption(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull LinxHomozygousDisruption homozygousDisruption) {
        return null;
    }

    @Nullable
    public static KnownCopyNumber findForDisruption(@NotNull Iterable<KnownCopyNumber> knownCopyNumbers,
            @NotNull LinxDisruption disruption) {
        return null;
    }
}
