package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Fusion;

import org.jetbrains.annotations.NotNull;

public class FusionComparator implements Comparator<Fusion> {

    private static final DriverComparator DRIVER_COMPARATOR = new DriverComparator();

    @Override
    public int compare(@NotNull Fusion fusion1, @NotNull Fusion fusion2) {
        int driverCompare = DRIVER_COMPARATOR.compare(fusion1, fusion2);
        if (driverCompare != 0) {
            return driverCompare;
        }

        int geneStartCompare = fusion1.geneStart().compareTo(fusion2.geneStart());
        if (geneStartCompare != 0) {
            return geneStartCompare;
        }

        int geneEndCompare = fusion1.geneEnd().compareTo(fusion2.geneEnd());
        if (geneEndCompare != 0) {
            return geneEndCompare;
        }

        int geneTranscriptStartCompare = fusion1.geneTranscriptStart().compareTo(fusion2.geneTranscriptStart());
        if (geneTranscriptStartCompare != 0) {
            return geneTranscriptStartCompare;
        }

        int geneTranscriptEndCompare = fusion1.geneTranscriptEnd().compareTo(fusion2.geneTranscriptEnd());
        if (geneTranscriptEndCompare != 0) {
            return geneTranscriptEndCompare;
        }

        int geneContextStartCompare = fusion1.geneContextStart().compareTo(fusion2.geneContextStart());
        if (geneContextStartCompare != 0) {
            return geneContextStartCompare;
        }

        return fusion1.geneContextEnd().compareTo(fusion2.geneContextEnd());
    }
}
