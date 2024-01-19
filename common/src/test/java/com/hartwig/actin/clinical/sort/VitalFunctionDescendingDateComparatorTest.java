package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunction;
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class VitalFunctionDescendingDateComparatorTest {

    @Test
    public void canSortVitalFunctions() {
        VitalFunction vitalFunction1 = withDate(LocalDate.of(2020, 1, 1));
        VitalFunction vitalFunction2 = withDate(LocalDate.of(2019, 1, 1));
        VitalFunction vitalFunction3 = withDate(LocalDate.of(2018, 1, 1));

        List<VitalFunction> vitalFunctions = Lists.newArrayList(vitalFunction3, vitalFunction1, vitalFunction2);

        vitalFunctions.sort(new VitalFunctionDescendingDateComparator());

        assertEquals(vitalFunction1, vitalFunctions.get(0));
        assertEquals(vitalFunction2, vitalFunctions.get(1));
        assertEquals(vitalFunction3, vitalFunctions.get(2));
    }

    @NotNull
    private static VitalFunction withDate(@NotNull LocalDate date) {
        return ImmutableVitalFunction.builder().date(date.atStartOfDay())
                .category(VitalFunctionCategory.HEART_RATE)
                .subcategory(Strings.EMPTY).value(0D).unit(Strings.EMPTY).valid(true)
                .build();
    }
}