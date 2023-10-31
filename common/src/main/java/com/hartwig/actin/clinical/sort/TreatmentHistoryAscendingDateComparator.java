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

        return Comparator.comparing(TreatmentHistoryEntry::startYear, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::startMonth, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparator::stopYearForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryAscendingDateComparator::stopMonthForHistoryEntry, nullSafeComparator)
                .thenComparing(TreatmentHistoryEntry::treatmentName)
                .compare(entry1, entry2);
    }

    @Nullable
    private static Integer stopYearForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return therapyDetailField(treatmentHistoryEntry, TreatmentHistoryDetails::stopYear);
    }

    @Nullable
    private static Integer stopMonthForHistoryEntry(@NotNull TreatmentHistoryEntry treatmentHistoryEntry) {
        return therapyDetailField(treatmentHistoryEntry, TreatmentHistoryDetails::stopMonth);
    }

    @Nullable
    private static Integer therapyDetailField(@NotNull TreatmentHistoryEntry treatmentHistoryEntry,
            @NotNull Function<TreatmentHistoryDetails, Integer> getField) {
        return (treatmentHistoryEntry.treatmentHistoryDetails() == null)
                ? null
                : getField.apply(treatmentHistoryEntry.treatmentHistoryDetails());
    }
}
