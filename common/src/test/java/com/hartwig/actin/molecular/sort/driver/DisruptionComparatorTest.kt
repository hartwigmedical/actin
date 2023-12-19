package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class DisruptionComparatorTest {

    @Test
    public void canSortDisruptions() {
        Disruption disruption1 = create("NF1", DriverLikelihood.HIGH, DisruptionType.DEL, 2D, 1D);
        Disruption disruption2 = create("APC", DriverLikelihood.LOW, DisruptionType.BND, 2D, 1D);
        Disruption disruption3 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 2D, 1D);
        Disruption disruption4 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1D, 0D);
        Disruption disruption5 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1D, 1D);

        List<Disruption> disruptions = Lists.newArrayList(disruption3, disruption5, disruption4, disruption2, disruption1);
        disruptions.sort(new DisruptionComparator());

        assertEquals(disruption1, disruptions.get(0));
        assertEquals(disruption2, disruptions.get(1));
        assertEquals(disruption3, disruptions.get(2));
        assertEquals(disruption4, disruptions.get(3));
        assertEquals(disruption5, disruptions.get(4));
    }

    @NotNull
    private static Disruption create(@NotNull String gene, @Nullable DriverLikelihood driverLikelihood, @NotNull DisruptionType type,
            double junctionCopyNumber, double undisruptedCopyNumber) {
        return TestDisruptionFactory.builder()
                .gene(gene)
                .driverLikelihood(driverLikelihood)
                .type(type)
                .junctionCopyNumber(junctionCopyNumber)
                .undisruptedCopyNumber(undisruptedCopyNumber)
                .build();
    }
}