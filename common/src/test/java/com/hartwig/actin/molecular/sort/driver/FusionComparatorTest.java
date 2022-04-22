package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableFusion;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class FusionComparatorTest {

    @Test
    public void canCompareFusions() {
        Fusion fusion1 = create(DriverLikelihood.HIGH, "EML4", "ALK", "exon 9 -> exon 9");
        Fusion fusion2 = create(DriverLikelihood.HIGH, "EML4", "ALK", "exon 8 -> exon 8");
        Fusion fusion3 = create(DriverLikelihood.LOW, "APC", "NTRK3", Strings.EMPTY);
        Fusion fusion4 = create(DriverLikelihood.LOW, "EML4", "ALK", "exon 1 -> exon 2");
        Fusion fusion5 = create(DriverLikelihood.LOW, "APC", "NTRK2", Strings.EMPTY);

        List<Fusion> fusions = Lists.newArrayList(fusion1, fusion2, fusion3, fusion4, fusion5);
        fusions.sort(new FusionComparator());

        assertEquals(fusion2, fusions.get(0));
        assertEquals(fusion1, fusions.get(1));
        assertEquals(fusion5, fusions.get(2));
        assertEquals(fusion3, fusions.get(3));
        assertEquals(fusion4, fusions.get(4));
    }

    @NotNull
    private static Fusion create(@NotNull DriverLikelihood driverLikelihood, @NotNull String fiveGene, @NotNull String threeGene,
            @NotNull String details) {
        return ImmutableFusion.builder()
                .event(Strings.EMPTY)
                .driverLikelihood(driverLikelihood)
                .fiveGene(fiveGene)
                .threeGene(threeGene)
                .details(details)
                .driverType(FusionDriverType.KNOWN)
                .build();
    }
}