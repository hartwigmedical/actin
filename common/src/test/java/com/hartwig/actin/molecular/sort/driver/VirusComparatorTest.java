package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VirusComparatorTest {

    @Test
    public void canCompareViruses() {
        Virus virus1 = create(DriverLikelihood.LOW, "HPV");
        Virus virus2 = create(DriverLikelihood.LOW, "EBV");
        Virus virus3 = create(DriverLikelihood.HIGH, "HPV");

        List<Virus> viruses = Lists.newArrayList(virus1, virus2, virus3);
        viruses.sort(new VirusComparator());

        assertEquals(virus3, viruses.get(0));
        assertEquals(virus2, viruses.get(1));
        assertEquals(virus1, viruses.get(2));
    }

    @NotNull
    private static Virus create(@NotNull DriverLikelihood driverLikelihood, @NotNull String name) {
        return TestVirusFactory.builder().driverLikelihood(driverLikelihood).name(name).integrations(0).build();
    }
}