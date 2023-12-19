package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HomozygousDisruptionComparatorTest {

    @Test
    public void canSortHomozygousDisruptions() {
        HomozygousDisruption homozygousDisruption1 = create("APC", DriverLikelihood.HIGH);
        HomozygousDisruption homozygousDisruption2 = create("NF1", DriverLikelihood.HIGH);
        HomozygousDisruption homozygousDisruption3 = create("APC", DriverLikelihood.LOW);

        List<HomozygousDisruption> disruptions = Lists.newArrayList(homozygousDisruption2, homozygousDisruption1, homozygousDisruption3);
        disruptions.sort(new HomozygousDisruptionComparator());

        assertEquals(homozygousDisruption1, disruptions.get(0));
        assertEquals(homozygousDisruption2, disruptions.get(1));
        assertEquals(homozygousDisruption3, disruptions.get(2));
    }

    @NotNull
    private static HomozygousDisruption create(@NotNull String gene, @NotNull DriverLikelihood driverLikelihood) {
        return TestHomozygousDisruptionFactory.builder().driverLikelihood(driverLikelihood).gene(gene).build();
    }
}