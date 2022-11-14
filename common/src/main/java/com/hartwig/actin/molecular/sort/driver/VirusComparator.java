package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirusComparator implements Comparator<Virus> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull Virus virus1, @NotNull Virus virus2) {
        int driverCompare = DRIVER_COMPARATOR.compare(virus1, virus2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int interpretationCompare = stringCompare(virus1.interpretation(), virus2.interpretation());
        if (interpretationCompare != 0) {
            return interpretationCompare;
        }

        return virus1.name().compareTo(virus2.name());
    }

    private static int stringCompare(@Nullable String string1, @Nullable String string2) {
        if (string1 == null && string2 == null) {
            return 0;
        } else if (string1 == null) {
            return 1;
        } else if (string2 == null) {
            return -1;
        } else {
            return string1.compareTo(string2);
        }
    }
}
