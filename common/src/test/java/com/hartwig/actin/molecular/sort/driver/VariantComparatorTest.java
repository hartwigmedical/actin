package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableVariant;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.VariantDriverType;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VariantComparatorTest {

    @Test
    public void canSortVariants() {
        Variant variant1 = create(DriverLikelihood.MEDIUM, "ATM", "ATM variant 1");
        Variant variant2 = create(DriverLikelihood.MEDIUM, "ATM", "ATM variant 2");
        Variant variant3 = create(DriverLikelihood.MEDIUM, "APC", "APC frameshift");
        Variant variant4 = create(DriverLikelihood.HIGH, "BRAF", "BRAF variant 1");
        Variant variant5 = create(DriverLikelihood.HIGH, "BRAF", "BRAF variant 2");

        List<Variant> variants = Lists.newArrayList(variant1, variant2, variant3, variant4, variant5);
        variants.sort(new VariantComparator());

        assertEquals(variant4, variants.get(0));
        assertEquals(variant5, variants.get(1));
        assertEquals(variant3, variants.get(2));
        assertEquals(variant1, variants.get(3));
        assertEquals(variant2, variants.get(4));
    }

    @NotNull
    private static Variant create(@NotNull DriverLikelihood driverLikelihood, @NotNull String gene, @NotNull String event) {
        return ImmutableVariant.builder()
                .event(event)
                .driverLikelihood(driverLikelihood)
                .gene(gene)
                .impact(Strings.EMPTY)
                .variantCopyNumber(0D)
                .totalCopyNumber(0D)
                .driverType(VariantDriverType.VUS)
                .clonalLikelihood(0D)
                .build();
    }
}