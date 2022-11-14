package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionComparatorTest {

    @Test
    public void canSortDisruptions() {
        Disruption disruption1 = create("NF1", DriverLikelihood.HIGH, "DEL");
        Disruption disruption2 = create("APC", DriverLikelihood.LOW, "BND");
        Disruption disruption3 = create("NF1", DriverLikelihood.LOW, "DUP");

        List<Disruption> disruptions = Lists.newArrayList(disruption3, disruption2, disruption1);
        disruptions.sort(new DisruptionComparator());

        assertEquals(disruption1, disruptions.get(0));
        assertEquals(disruption2, disruptions.get(1));
        assertEquals(disruption3, disruptions.get(2));
    }

    @NotNull
    private static Disruption create(@NotNull String gene, @NotNull DriverLikelihood driverLikelihood, @NotNull String type) {
        return TestDisruptionFactory.builder().gene(gene).driverLikelihood(driverLikelihood).type(type).build();
    }
}