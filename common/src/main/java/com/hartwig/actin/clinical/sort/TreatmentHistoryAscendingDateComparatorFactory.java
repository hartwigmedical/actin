package com.hartwig.actin.clinical.sort;

import java.util.Comparator;
import java.util.function.Function;

import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreatmentHistoryAscendingDateComparatorFactory {

    public static Comparator<TreatmentHistoryEntry> treatmentHistoryEntryComparator() {
        Comparator<Integer> nullSafeComparator = Comparator.nullsFirst(Comparator.naturalOrder());
        return Comparator.comparing(TreatmentHistoryEntry::startYear, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::startMonth, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparatorFactory::stopYearForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparatorFactory::stopMonthForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::treatmentName);
    }

    @Nullable
    private static Integer stopYearForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return therapyDetailField(treatmentHistoryEntry, TherapyHistoryDetails::stopYear);
    }

    @Nullable
    private static Integer stopMonthForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return therapyDetailField(treatmentHistoryEntry, TherapyHistoryDetails::stopMonth);
    }
   
    @Nullable
    private static Integer therapyDetailField(@NotNull TreatmentHistoryEntry treatmentHistoryEntry,
            @NotNull Function<TherapyHistoryDetails, Integer> getField) {
        return (treatmentHistoryEntry.therapyHistoryDetails() == null)
                ? null
                : getField.apply(treatmentHistoryEntry.therapyHistoryDetails());
    }
}
