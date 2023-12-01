package com.hartwig.actin.clinical.sort;

import java.util.Comparator;
import java.util.function.Function;

import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreatmentHistoryAscendingDateComparator implements Comparator<TreatmentHistoryEntry> {

    @Override
    public int compare(@NotNull TreatmentHistoryEntry entry1, @NotNull TreatmentHistoryEntry entry2) {
        Comparator<Integer> nullSafeComparator = Comparator.nullsLast(Comparator.naturalOrder());
        Integer startYear1 = entry1.startYear();
        Integer startYear2 = entry2.startYear();
        Integer stopYear1 = stopYearForHistoryEntry(entry1);
        Integer stopYear2 = stopYearForHistoryEntry(entry2);

        if ((startYear1 == null && stopYear1 != null && startYear2 != null && (startYear2 >= stopYear1)) || (startYear2 == null
                && stopYear2 != null && startYear1 != null && (startYear1 >= stopYear2))) {
            return Comparator.comparing(TreatmentHistoryAscendingDateComparator::stopYearForHistoryEntry, nullSafeComparator)
                    .thenComparing(TreatmentHistoryAscendingDateComparator::stopMonthForHistoryEntry, nullSafeComparator)
                    .thenComparing(TreatmentHistoryEntry::treatmentName)
                    .compare(entry1, entry2);
        }
        return Comparator.comparing(TreatmentHistoryEntry::startYear, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::startMonth, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparator::stopYearForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparator::stopMonthForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::treatmentName)
                .compare(entry1, entry2);
    }

    @Nullable
    private static Integer stopYearForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return treatmentDetailField(treatmentHistoryEntry, TreatmentHistoryDetails::stopYear);
    }

    @Nullable
    private static Integer stopMonthForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return treatmentDetailField(treatmentHistoryEntry, TreatmentHistoryDetails::stopMonth);
    }

    @Nullable
    private static Integer treatmentDetailField(@NotNull TreatmentHistoryEntry treatmentHistoryEntry,
            @NotNull Function<TreatmentHistoryDetails, Integer> getField) {
        return (treatmentHistoryEntry.treatmentHistoryDetails() == null)
                ? null
                : getField.apply(treatmentHistoryEntry.treatmentHistoryDetails());
    }
}
