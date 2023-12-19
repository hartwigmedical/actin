package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.GeneAlteration;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class GeneAlterationComparatorTest {

    @Test
    public void canSortGeneAlterations() {
        GeneAlteration alteration1 = create("gene A", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION);
        GeneAlteration alteration2 = create("gene A", GeneRole.TSG, ProteinEffect.GAIN_OF_FUNCTION);
        GeneAlteration alteration3= create("gene A", GeneRole.TSG, ProteinEffect.NO_EFFECT);
        GeneAlteration alteration4 = create("gene B", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION);

        List<GeneAlteration> alterations = Lists.newArrayList(alteration2, alteration1, alteration4, alteration3);
        alterations.sort(new GeneAlterationComparator());

        assertEquals(alteration1, alterations.get(0));
        assertEquals(alteration2, alterations.get(1));
        assertEquals(alteration3, alterations.get(2));
        assertEquals(alteration4, alterations.get(3));
    }

    @NotNull
    private static GeneAlteration create(@NotNull String gene, @NotNull GeneRole geneRole, @NotNull ProteinEffect proteinEffect) {
        return new GeneAlteration() {
            @NotNull
            @Override
            public String gene() {
                return gene;
            }

            @NotNull
            @Override
            public GeneRole geneRole() {
                return geneRole;
            }

            @NotNull
            @Override
            public ProteinEffect proteinEffect() {
                return proteinEffect;
            }

            @Nullable
            @Override
            public Boolean isAssociatedWithDrugResistance() {
                return null;
            }

            @Override
            public String toString() {
                return gene + " " + geneRole + " " + proteinEffect;
            }
        };
    }
}