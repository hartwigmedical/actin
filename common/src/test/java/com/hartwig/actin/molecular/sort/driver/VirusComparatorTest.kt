package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.driver.Virus;
import com.hartwig.actin.molecular.datamodel.driver.VirusType;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VirusComparatorTest {

    @Test
    public void canSortViruses() {
        Virus virus1 = create(DriverLikelihood.HIGH, "Human 16", VirusType.HUMAN_PAPILLOMA_VIRUS);
        Virus virus2 = create(DriverLikelihood.LOW, "Epstein 1", VirusType.EPSTEIN_BARR_VIRUS);
        Virus virus3 = create(DriverLikelihood.LOW, "Human 1", VirusType.HUMAN_PAPILLOMA_VIRUS);
        Virus virus4 = create(DriverLikelihood.LOW, "Human 2", VirusType.HUMAN_PAPILLOMA_VIRUS);

        List<Virus> viruses = Lists.newArrayList(virus2, virus4, virus1, virus3);
        viruses.sort(new VirusComparator());

        assertEquals(virus1, viruses.get(0));
        assertEquals(virus2, viruses.get(1));
        assertEquals(virus3, viruses.get(2));
        assertEquals(virus4, viruses.get(3));
    }

    @NotNull
    private static Virus create(@NotNull DriverLikelihood driverLikelihood, @NotNull String name, @NotNull VirusType type) {
        return TestVirusFactory.builder().driverLikelihood(driverLikelihood).name(name).type(type).build();
    }
}