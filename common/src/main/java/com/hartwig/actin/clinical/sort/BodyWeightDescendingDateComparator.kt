package com.hartwig.actin.clinical.sort;

import java.util.Comparator;

import com.hartwig.actin.clinical.datamodel.BodyWeight;

import org.jetbrains.annotations.NotNull;

public class BodyWeightDescendingDateComparator implements Comparator<BodyWeight> {

    @Override
    public int compare(@NotNull BodyWeight bodyWeight1, @NotNull BodyWeight bodyWeight2) {
        int dateCompare = bodyWeight2.date().compareTo(bodyWeight1.date());
        if (dateCompare != 0) {
            return dateCompare;
        }

        int valueCompare = Double.compare(bodyWeight2.value(), bodyWeight1.value());
        if (valueCompare != 0) {
            return valueCompare;
        }

        return bodyWeight1.unit().compareTo(bodyWeight2.unit());
    }
}
