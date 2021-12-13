package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodPressure;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class BloodPressureDescendingDateComparatorTest {

    @Test
    public void canSortBloodPressures() {
        BloodPressure bloodPressure1 = withDate(LocalDate.of(2020, 1, 1));
        BloodPressure bloodPressure2 = withDate(LocalDate.of(2019, 1, 1));
        BloodPressure bloodPressure3 = withDate(LocalDate.of(2018, 1, 1));

        List<BloodPressure> bloodPressures = Lists.newArrayList(bloodPressure3, bloodPressure1, bloodPressure2);

        bloodPressures.sort(new BloodPressureDescendingDateComparator());

        assertEquals(bloodPressure1, bloodPressures.get(0));
        assertEquals(bloodPressure2, bloodPressures.get(1));
        assertEquals(bloodPressure3, bloodPressures.get(2));
    }

    @NotNull
    private static BloodPressure withDate(@NotNull LocalDate date) {
        return ImmutableBloodPressure.builder().date(date).category(Strings.EMPTY).value(0D).unit(Strings.EMPTY).build();
    }
}