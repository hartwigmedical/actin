package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VariantComparatorTest {

    @Test
    public void canSortVariants() {
        Variant variant1 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1800");
        Variant variant2 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1801");
        Variant variant3 = create(DriverLikelihood.HIGH, "BRAF", "V601E", "1800");
        Variant variant4 = create(DriverLikelihood.HIGH, "NTRK", "V601E", "1800");
        Variant variant5 = create(DriverLikelihood.MEDIUM, "BRAF", "V600E", "1800");


        List<Variant> variants = Lists.newArrayList(variant3, variant5, variant1, variant4, variant2);
        variants.sort(new VariantComparator());

        assertEquals(variant1, variants.get(0));
        assertEquals(variant2, variants.get(1));
        assertEquals(variant3, variants.get(2));
        assertEquals(variant4, variants.get(3));
        assertEquals(variant5, variants.get(4));
    }

    @NotNull
    private static Variant create(@NotNull DriverLikelihood driverLikelihood, @NotNull String gene, @NotNull String hgvsProteinImpact,
            @NotNull String hgvsCodingImpact) {
        TranscriptImpact canonicalImpact =
                TestTranscriptImpactFactory.builder().hgvsProteinImpact(hgvsProteinImpact).hgvsCodingImpact(hgvsCodingImpact).build();
        return TestVariantFactory.builder().driverLikelihood(driverLikelihood).gene(gene).canonicalImpact(canonicalImpact).build();
    }
}