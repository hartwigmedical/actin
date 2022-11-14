package com.hartwig.actin.report.interpretation;

import java.util.Comparator;

import com.hartwig.actin.molecular.sort.driver.DriverLikelihoodComparator;

import org.jetbrains.annotations.NotNull;

public class MolecularDriverEntryComparator implements Comparator<MolecularDriverEntry> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();
    private static final DriverTypeComparator DRIVER_TYPE_COMPARATOR = new DriverTypeComparator();

    @Override
    public int compare(@NotNull MolecularDriverEntry entry1, @NotNull MolecularDriverEntry entry2) {
        int driverLikelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(entry1.driverLikelihood(), entry2.driverLikelihood());

        if (driverLikelihoodCompare != 0) {
            return driverLikelihoodCompare;
        }

        int driverTypeCompare = DRIVER_TYPE_COMPARATOR.compare(entry1.driverType(), entry2.driverType());
        if (driverTypeCompare != 0) {
            return driverTypeCompare;
        }

        return entry1.driver().compareTo(entry2.driver());
    }

    private static class DriverTypeComparator implements Comparator<String> {

        @Override
        public int compare(@NotNull String string1, @NotNull String string2) {
            String type1 = string1.toLowerCase();
            String type2 = string2.toLowerCase();

            int mutationCompare = Boolean.compare(type2.startsWith("mutation"), type1.startsWith("mutation"));
            if (mutationCompare != 0) {
                return mutationCompare;
            }

            int amplificationCompare = Boolean.compare(type2.startsWith("amplification"), type1.startsWith("amplification"));
            if (amplificationCompare != 0) {
                return amplificationCompare;
            }

            int lossCompare = Boolean.compare(type2.startsWith("loss"), type1.startsWith("loss"));
            if (lossCompare != 0) {
                return lossCompare;
            }

            int fusionCompare = Boolean.compare(type2.contains("fusion"), type1.contains("fusion"));
            if (fusionCompare != 0) {
                return fusionCompare;
            }

            int disruptionCompare = Boolean.compare(type2.contains("disruption"), type1.contains("disruption"));
            if (disruptionCompare != 0) {
                return disruptionCompare;
            }

            int virusCompare = Boolean.compare(type2.startsWith("virus"), type1.startsWith("virus"));
            if (virusCompare != 0) {
                return virusCompare;
            }

            return type1.compareTo(type2);
        }
    }
}
