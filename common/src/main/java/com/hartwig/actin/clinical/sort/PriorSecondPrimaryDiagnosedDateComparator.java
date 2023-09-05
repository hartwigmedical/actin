package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.jetbrains.annotations.NotNull;

public class PriorSecondPrimaryDiagnosedDateComparator implements Comparator<PriorSecondPrimary> {

    @Override
    public int compare(@NotNull PriorSecondPrimary secondPrimary1, @NotNull PriorSecondPrimary secondPrimary2) {
        Comparator<Integer> nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder());

        return Comparator.comparing(PriorSecondPrimary::diagnosedYear, nullSafeComparator)
                .thenComparing(PriorSecondPrimary::diagnosedMonth, nullSafeComparator)
                .thenComparing(PriorSecondPrimary::tumorLocation)
                .compare(secondPrimary1, secondPrimary2);
    }
}
