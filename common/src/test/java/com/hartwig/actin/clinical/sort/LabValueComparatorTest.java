package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LabValueComparatorTest {

    @Test
    public void canSortLabValues() {
        ImmutableLabValue.Builder builder =
                ImmutableLabValue.builder().name(Strings.EMPTY).comparator(Strings.EMPTY).value(0D).unit(Strings.EMPTY);

        LabValue value1 = builder.date(LocalDate.of(2018, 1, 1)).code("Y").build();
        LabValue value2 = builder.date(LocalDate.of(2019, 1, 1)).code("X").build();
        LabValue value3 = builder.date(LocalDate.of(2018, 1, 1)).code("X").build();
        List<LabValue> values = Lists.newArrayList(value1, value2, value3);

        values.sort(new LabValueComparator());

        assertEquals(value2, values.get(0));
        assertEquals(value3, values.get(1));
        assertEquals(value1, values.get(2));
    }

}