package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.BloodPressure;

import org.jetbrains.annotations.NotNull;

public class BloodPressureDescendingDateComparator implements Comparator<BloodPressure> {

    @Override
    public int compare(@NotNull BloodPressure bloodPressure1, @NotNull BloodPressure bloodPressure2) {
        int dateCompare = bloodPressure2.date().compareTo(bloodPressure1.date());
        if (dateCompare != 0) {
            return dateCompare;
        }

        int categoryCompare = bloodPressure1.category().compareTo(bloodPressure2.category());
        if (categoryCompare != 0) {
            return categoryCompare;
        }

        int unitCompare = bloodPressure1.unit().compareTo(bloodPressure2.unit());
        if (unitCompare != 0) {
            return unitCompare;
        }

        return Double.compare(bloodPressure1.value(), bloodPressure2.value());
    }
}
