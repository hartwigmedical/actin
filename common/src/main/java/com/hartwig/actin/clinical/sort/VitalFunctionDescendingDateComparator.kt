package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.VitalFunction;

import org.jetbrains.annotations.NotNull;

public class VitalFunctionDescendingDateComparator implements Comparator<VitalFunction> {

    @Override
    public int compare(@NotNull VitalFunction vitalFunction1, @NotNull VitalFunction vitalFunction2) {
        int dateCompare = vitalFunction2.date().compareTo(vitalFunction1.date());
        if (dateCompare != 0) {
            return dateCompare;
        }

        int categoryCompare = vitalFunction1.category().compareTo(vitalFunction2.category());
        if (categoryCompare != 0) {
            return categoryCompare;
        }

        int subcategoryCompare = vitalFunction1.subcategory().compareTo(vitalFunction2.subcategory());
        if (subcategoryCompare != 0) {
            return subcategoryCompare;
        }

        int unitCompare = vitalFunction1.unit().compareTo(vitalFunction2.unit());
        if (unitCompare != 0) {
            return unitCompare;
        }

        return Double.compare(vitalFunction1.value(), vitalFunction2.value());
    }
}
