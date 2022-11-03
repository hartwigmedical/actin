package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VariantComparatorTest {

    @Test
    public void canSortVariants() {
        Variant variant1 = create(DriverLikelihood.MEDIUM, "ATM");
        Variant variant2 = create(DriverLikelihood.MEDIUM, "ATM");
        Variant variant3 = create(DriverLikelihood.MEDIUM, "APC");
        Variant variant4 = create(DriverLikelihood.HIGH, "BRAF");
        Variant variant5 = create(DriverLikelihood.HIGH, "BRAF");

        List<Variant> variants = Lists.newArrayList(variant1, variant2, variant3, variant4, variant5);
        variants.sort(new VariantComparator());

        assertEquals(variant4, variants.get(0));
        assertEquals(variant5, variants.get(1));
        assertEquals(variant3, variants.get(2));
        assertEquals(variant1, variants.get(3));
        assertEquals(variant2, variants.get(4));
    }

    @NotNull
    private static Variant create(@NotNull DriverLikelihood driverLikelihood, @NotNull String gene) {
        return TestVariantFactory.builder().driverLikelihood(driverLikelihood).gene(gene).build();
    }
}