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
    public void canCompareDisruptions() {
        Disruption disruption1 = create("NF1", DriverLikelihood.LOW, "DUP", "intron 1");
        Disruption disruption2 = create("NF1", DriverLikelihood.LOW, "DUP", "intron 3");
        Disruption disruption3 = create("NF1", DriverLikelihood.HIGH, "DEL", "intron 2");
        Disruption disruption4 = create("APC", DriverLikelihood.LOW, "BND", "intron 5");

        List<Disruption> disruptions = Lists.newArrayList(disruption1, disruption2, disruption3, disruption4);
        disruptions.sort(new DisruptionComparator());

        assertEquals(disruption3, disruptions.get(0));
        assertEquals(disruption4, disruptions.get(1));
        assertEquals(disruption1, disruptions.get(2));
        assertEquals(disruption2, disruptions.get(3));
    }

    @NotNull
    private static Disruption create(@NotNull String gene, @NotNull DriverLikelihood driverLikelihood, @NotNull String type,
            @NotNull String range) {
        return TestDisruptionFactory.builder().driverLikelihood(driverLikelihood).gene(gene).type(type).range(range).build();
    }
}