package com.hartwig.actin.molecular.sort;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;

import org.jetbrains.annotations.NotNull;

public class CopyNumberComparator implements Comparator<CopyNumberDriver> {

    @Override
    public int compare(@NotNull CopyNumberDriver copyNumberDriver1, @NotNull CopyNumberDriver copyNumberDriver2) {
        return copyNumberDriver1.gene().compareTo(copyNumberDriver2.gene());
    }
}
