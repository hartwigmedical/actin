package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

public class VirusComparator implements Comparator<Virus> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull Virus virus1, @NotNull Virus virus2) {
        int driverCompare = DRIVER_COMPARATOR.compare(virus1, virus2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int nameCompare = virus1.name().compareTo(virus2.name());
        if (nameCompare != 0) {
            return nameCompare;
        }

        // TODO
        return 0;
    }
}
