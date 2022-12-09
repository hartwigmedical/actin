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
        PriorTumorTreatment treatment1 = create("treatment 1", null, null, null, null);
        PriorTumorTreatment treatment2 = create("treatment 1", 2018, null, null, null);
        PriorTumorTreatment treatment3 = create("treatment 1", 2020, 2, null, null);
        PriorTumorTreatment treatment4 = create("treatment 1", 2020, 3, null, null);
        PriorTumorTreatment treatment5 = create("treatment 1", 2020, 3, 2021, null);
        PriorTumorTreatment treatment6 = create("treatment 1", 2020, 3, 2021, 6);
        PriorTumorTreatment treatment7 = create("treatment 2", 2020, 3, 2021, 6);
        PriorTumorTreatment treatment8 = create("treatment 1", 2021, 1, null, null);

        List<PriorTumorTreatment> treatments =
                Lists.newArrayList(treatment8, treatment6, treatment3, treatment7, treatment4, treatment1, treatment5, treatment2);
        treatments.sort(new PriorTumorTreatmentDescendingDateComparator());

        assertEquals(treatment1, treatments.get(0));
        assertEquals(treatment2, treatments.get(1));
        assertEquals(treatment3, treatments.get(2));
        assertEquals(treatment4, treatments.get(3));
        assertEquals(treatment5, treatments.get(4));
        assertEquals(treatment6, treatments.get(5));
        assertEquals(treatment7, treatments.get(6));
        assertEquals(treatment8, treatments.get(7));
    }

    @NotNull
    private static PriorTumorTreatment create(@NotNull String name, @Nullable Integer startYear,
            @Nullable Integer startMonth, @Nullable Integer stopYear, @Nullable Integer stopMonth) {
        return ImmutablePriorTumorTreatment.builder()
                .name(name)
                .startYear(startYear)
                .startMonth(startMonth)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .isSystemic(false)
                .build();
    }
}