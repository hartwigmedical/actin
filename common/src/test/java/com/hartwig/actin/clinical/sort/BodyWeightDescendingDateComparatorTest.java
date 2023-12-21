package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class BodyWeightDescendingDateComparatorTest {

    @Test
    public void canSortBodyWeights() {
        BodyWeight weight1 = builder().date(LocalDate.of(2020, 4, 4).atStartOfDay()).value(0D).unit(Strings.EMPTY).build();
        BodyWeight weight2 = builder().date(LocalDate.of(2020, 4, 4).atStartOfDay()).value(80D).unit("unit 1").build();
        BodyWeight weight3 = builder().date(LocalDate.of(2020, 4, 4).atStartOfDay()).value(80D).unit("unit 2").build();
        BodyWeight weight4 = builder().date(LocalDate.of(2021, 4, 4).atStartOfDay()).build();

        List<BodyWeight> weights = Lists.newArrayList(weight1, weight2, weight4, weight3);

        weights.sort(new BodyWeightDescendingDateComparator());

        assertEquals(weight4, weights.get(0));
        assertEquals(weight2, weights.get(1));
        assertEquals(weight3, weights.get(2));
        assertEquals(weight1, weights.get(3));
    }

    @NotNull
    private static ImmutableBodyWeight.Builder builder() {
        return ImmutableBodyWeight.builder().value(0D).unit(Strings.EMPTY);
    }
}