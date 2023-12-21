package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LabValueDescendingDateComparatorTest {

    @Test
    public void canSortLabValues() {
        ImmutableLabValue.Builder builder = ImmutableLabValue.builder().name(Strings.EMPTY).comparator(Strings.EMPTY).unit(LabUnit.NONE);

        LabValue value1 = builder.date(LocalDate.of(2018, 1, 1)).code("Y").value(0D).build();
        LabValue value2 = builder.date(LocalDate.of(2019, 1, 1)).code("X").value(0D).build();
        LabValue value3 = builder.date(LocalDate.of(2018, 1, 1)).code("X").value(1D).build();
        LabValue value4 = builder.date(LocalDate.of(2018, 1, 1)).code("X").value(2D).build();
        List<LabValue> values = Lists.newArrayList(value1, value2, value3, value4);

        values.sort(new LabValueDescendingDateComparator());

        assertEquals(value2, values.get(0));
        assertEquals(value4, values.get(1));
        assertEquals(value3, values.get(2));
        assertEquals(value1, values.get(3));
    }
}