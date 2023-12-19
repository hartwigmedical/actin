package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;

import org.jetbrains.annotations.NotNull;

public class GeneAlterationComparator implements Comparator<GeneAlteration> {

    @Override
    public int compare(@NotNull GeneAlteration alteration1, @NotNull GeneAlteration alteration2) {
        int geneCompare = alteration1.gene().compareTo(alteration2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        int geneRoleCompare = alteration1.geneRole().toString().compareTo(alteration2.geneRole().toString());
        if (geneRoleCompare != 0) {
            return geneRoleCompare;
        }

        return alteration1.proteinEffect().toString().compareTo(alteration2.proteinEffect().toString());
    }
}
