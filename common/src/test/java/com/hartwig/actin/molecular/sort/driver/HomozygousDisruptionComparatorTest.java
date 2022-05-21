package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableHomozygousDisruption;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HomozygousDisruptionComparatorTest {

    @Test
    public void canCompareHomozygousDisruptions() {
        HomozygousDisruption homozygousDisruption1 = create("NF1", DriverLikelihood.HIGH);
        HomozygousDisruption homozygousDisruption2 = create("APC", DriverLikelihood.HIGH);
        HomozygousDisruption homozygousDisruption3 = create("APC", DriverLikelihood.LOW);

        List<HomozygousDisruption> disruptions = Lists.newArrayList(homozygousDisruption1, homozygousDisruption2, homozygousDisruption3);
        disruptions.sort(new HomozygousDisruptionComparator());

        assertEquals(homozygousDisruption2, disruptions.get(0));
        assertEquals(homozygousDisruption1, disruptions.get(1));
        assertEquals(homozygousDisruption3, disruptions.get(2));
    }

    @NotNull
    private static HomozygousDisruption create(@NotNull String gene, @NotNull DriverLikelihood driverLikelihood) {
        return ImmutableHomozygousDisruption.builder().event(Strings.EMPTY).driverLikelihood(driverLikelihood).gene(gene).build();
    }
}