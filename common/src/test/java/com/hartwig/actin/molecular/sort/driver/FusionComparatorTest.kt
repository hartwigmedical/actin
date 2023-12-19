package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class FusionComparatorTest {

    @Test
    public void canSortFusions() {
        Fusion fusion1 = create(DriverLikelihood.HIGH, "EML4", "ALK");
        Fusion fusion2 = create(DriverLikelihood.LOW, "APC", "NTRK2");
        Fusion fusion3 = create(DriverLikelihood.LOW, "APC", "NTRK3");
        Fusion fusion4 = create(DriverLikelihood.LOW, "EML4", "ALK");

        List<Fusion> fusions = Lists.newArrayList(fusion3, fusion2, fusion4, fusion1);
        fusions.sort(new FusionComparator());

        assertEquals(fusion1, fusions.get(0));
        assertEquals(fusion2, fusions.get(1));
        assertEquals(fusion3, fusions.get(2));
        assertEquals(fusion4, fusions.get(3));
    }

    @NotNull
    private static Fusion create(@NotNull DriverLikelihood driverLikelihood, @NotNull String geneStart, @NotNull String geneEnd) {
        return TestFusionFactory.builder()
                .driverLikelihood(driverLikelihood)
                .geneStart(geneStart)
                .geneEnd(geneEnd)
                .build();
    }
}