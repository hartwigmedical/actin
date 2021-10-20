package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class LabValueComparator implements Comparator<LabValue> {

    @Override
    public int compare(@NotNull LabValue lab1, @NotNull LabValue lab2) {
        if (lab1.date().equals(lab2.date())) {
            return lab1.code().compareTo(lab2.code());
        }

        return lab2.date().compareTo(lab1.date());
    }
}
