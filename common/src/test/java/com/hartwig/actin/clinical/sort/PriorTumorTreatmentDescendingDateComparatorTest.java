package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class PriorTumorTreatmentDescendingDateComparatorTest {

    @Test
    public void canSortOnDescendingDate() {
        PriorTumorTreatment treatment1 = withNameYearMonth("1", null, null);
        PriorTumorTreatment treatment2 = withNameYearMonth("1", 2020, 2);
        PriorTumorTreatment treatment3 = withNameYearMonth("2", 2020, 3);
        PriorTumorTreatment treatment4 = withNameYearMonth("2", 2021, 1);
        PriorTumorTreatment treatment5 = withNameYearMonth("2", 2018, null);

        List<PriorTumorTreatment> treatments = Lists.newArrayList(treatment1, treatment2, treatment3, treatment4, treatment5);
        treatments.sort(new PriorTumorTreatmentDescendingDateComparator());

        assertEquals(treatment5, treatments.get(0));
        assertEquals(treatment2, treatments.get(1));
        assertEquals(treatment3, treatments.get(2));
        assertEquals(treatment4, treatments.get(3));
        assertEquals(treatment1, treatments.get(4));
    }

    @NotNull
    private static PriorTumorTreatment withNameYearMonth(@NotNull String name, @Nullable Integer year, @Nullable Integer month) {
        return ImmutablePriorTumorTreatment.builder().name(name).year(year).month(month).isSystemic(false).build();
    }
}