package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumberDriver> {

    @Override
    public int compare(@NotNull CopyNumberDriver copyNumberDriver1, @NotNull CopyNumberDriver copyNumberDriver2) {
        int geneCompare = copyNumberDriver1.gene().compareTo(copyNumberDriver2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        return copyNumberDriver1.event().compareTo(copyNumberDriver2.event());
    }
}
