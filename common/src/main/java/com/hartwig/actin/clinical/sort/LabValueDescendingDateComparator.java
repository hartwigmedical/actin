package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.google.common.primitives.Doubles;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class LabValueDescendingDateComparator implements Comparator<LabValue> {

    @Override
    public int compare(@NotNull LabValue lab1, @NotNull LabValue lab2) {
        // Descending on date
        int dateCompare = lab2.date().compareTo(lab1.date());
        if (dateCompare != 0) {
            return dateCompare;
        }

        int codeCompare = lab1.code().compareTo(lab2.code());
        if (codeCompare != 0) {
            return codeCompare;
        }

        // In case a code has been measured twice on the same date -> put highest value first.
        return Doubles.compare(lab2.value(), lab1.value());
    }
}
