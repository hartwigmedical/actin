package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Disruption;

import org.jetbrains.annotations.NotNull;

public class DisruptionComparator implements Comparator<Disruption> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    private static final GeneAlterationComparator GENE_ALTERATION_COMPARATOR = new GeneAlterationComparator();

    @Override
    public int compare(@NotNull Disruption disruption1, @NotNull Disruption disruption2) {
        int driverCompare = DRIVER_COMPARATOR.compare(disruption1, disruption2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneAlterationCompare = GENE_ALTERATION_COMPARATOR.compare(disruption1, disruption2);
        if (geneAlterationCompare != 0) {
            return geneAlterationCompare;
        }

        int typeCompare = disruption1.type().toString().compareTo(disruption2.type().toString());
        if (typeCompare != 0) {
            return typeCompare;
        }

        int junctionCompare = Double.compare(disruption2.junctionCopyNumber(), disruption1.junctionCopyNumber());
        if (junctionCompare != 0) {
            return junctionCompare;
        }

        return Double.compare(disruption1.undisruptedCopyNumber(), disruption2.undisruptedCopyNumber());
    }
}
