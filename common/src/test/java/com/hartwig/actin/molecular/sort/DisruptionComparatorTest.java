package com.hartwig.actin.molecular.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableDisruption;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionComparatorTest {

    @Test
    public void canCompareDisruptions() {
        Disruption disruption1 = create("NF1", false);
        Disruption disruption2 = create("NF1", true);
        Disruption disruption3 = create("APC", false);

        List<Disruption> disruptions = Lists.newArrayList(disruption1, disruption2, disruption3);
        disruptions.sort(new DisruptionComparator());

        assertEquals(disruption2, disruptions.get(0));
        assertEquals(disruption3, disruptions.get(1));
        assertEquals(disruption1, disruptions.get(2));
    }

    @NotNull
    private static Disruption create(@NotNull String gene, boolean isHomozygous) {
        return ImmutableDisruption.builder().event(Strings.EMPTY).gene(gene).isHomozygous(isHomozygous).details(Strings.EMPTY).build();
    }

}