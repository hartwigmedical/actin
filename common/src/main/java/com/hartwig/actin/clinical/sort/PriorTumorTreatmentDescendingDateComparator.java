package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PriorTumorTreatmentDescendingDateComparator implements Comparator<PriorTumorTreatment> {

    @Override
    public int compare(@NotNull PriorTumorTreatment priorTumorTreatment1, @NotNull PriorTumorTreatment priorTumorTreatment2) {
        int startYearCompare = intCompare(priorTumorTreatment1.startYear(), priorTumorTreatment2.startYear());
        if (startYearCompare != 0) {
            return startYearCompare;
        }

        int startMonthCompare = intCompare(priorTumorTreatment1.startMonth(), priorTumorTreatment2.startMonth());
        if (startMonthCompare != 0) {
            return startMonthCompare;
        }

        int stopYearCompare = intCompare(priorTumorTreatment1.stopYear(), priorTumorTreatment2.stopYear());
        if (stopYearCompare != 0) {
            return stopYearCompare;
        }

        int stopMonthCompare = intCompare(priorTumorTreatment1.stopMonth(), priorTumorTreatment2.stopMonth());
        if (stopMonthCompare != 0) {
            return stopMonthCompare;
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
            return -1;
        } else if (int2 == null) {
            return 1;
        } else {
            return int1.compareTo(int2);
        }
    }
}
