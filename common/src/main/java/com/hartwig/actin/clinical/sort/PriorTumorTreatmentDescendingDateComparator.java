package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PriorTumorTreatmentDescendingDateComparator implements Comparator<PriorTumorTreatment> {

    @Override
    public int compare(@NotNull PriorTumorTreatment priorTumorTreatment1, @NotNull PriorTumorTreatment priorTumorTreatment2) {
        int yearCompare = intCompare(priorTumorTreatment1.startYear(), priorTumorTreatment2.startYear());
        if (yearCompare != 0) {
            return yearCompare;
        }

        int monthCompare = intCompare(priorTumorTreatment1.startMonth(), priorTumorTreatment2.startMonth());
        if (monthCompare != 0) {
            return monthCompare;
        }

        return toName(priorTumorTreatment1).compareTo(toName(priorTumorTreatment2));
    }

    @NotNull
    private static String toName(@NotNull PriorTumorTreatment priorTumorTreatment) {
        return !priorTumorTreatment.name().isEmpty()
                ? priorTumorTreatment.name()
                : TreatmentCategoryResolver.toStringList(priorTumorTreatment.categories());
    }

    private static int intCompare(@Nullable Integer int1, @Nullable Integer int2) {
        if (int1 == null && int2 == null) {
            return 0;
        } else if (int1 == null) {
            return 1;
        } else if (int2 == null) {
            return -1;
        } else {
            return int1.compareTo(int2);
        }
    }
}
