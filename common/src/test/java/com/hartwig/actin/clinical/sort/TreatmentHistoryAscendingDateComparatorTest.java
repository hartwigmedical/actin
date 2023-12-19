package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class TreatmentHistoryAscendingDateComparatorTest {

    @Test
    public void shouldSortByAscendingStartDateThenByAscendingEndDateThenByName() {
        TreatmentHistoryEntry treatment1 = create("treatment A", null, null, 2018, null);
        TreatmentHistoryEntry treatment2 = create("treatment A", 2018, null, null, null);
        TreatmentHistoryEntry treatment3 = create("treatment A", null, null, 2020, 1);
        TreatmentHistoryEntry treatment4 = create("treatment A", 2020, 2, null, null);
        TreatmentHistoryEntry treatment5 = create("treatment A", 2020, 3, 2021, 6);
        TreatmentHistoryEntry treatment6 = create("treatment B", 2020, 3, 2021, 6);
        TreatmentHistoryEntry treatment7 = create("treatment A", 2020, 3, 2021, null);
        TreatmentHistoryEntry treatment8 = create("treatment A", 2020, 3, null, null);
        TreatmentHistoryEntry treatment9 = create("treatment A", null, null, 2020, 4);
        TreatmentHistoryEntry treatment10 = create("treatment A", 2021, 1, null, null);
        TreatmentHistoryEntry treatment11 = create("treatment A", null, null, null, null);

        List<TreatmentHistoryEntry> treatments = Lists.newArrayList(treatment11,
                treatment10,
                treatment9,
                treatment8,
                treatment7,
                treatment6,
                treatment5,
                treatment4,
                treatment3,
                treatment2,
                treatment1);
        treatments.sort(new TreatmentHistoryAscendingDateComparator());

        assertEquals(treatment1, treatments.get(0));
        assertEquals(treatment2, treatments.get(1));
        assertEquals(treatment3, treatments.get(2));
        assertEquals(treatment4, treatments.get(3));
        assertEquals(treatment5, treatments.get(4));
        assertEquals(treatment6, treatments.get(5));
        assertEquals(treatment7, treatments.get(6));
        assertEquals(treatment8, treatments.get(7));
        assertEquals(treatment9, treatments.get(8));
        assertEquals(treatment10, treatments.get(9));
        assertEquals(treatment11, treatments.get(10));
    }

    @NotNull
    private static TreatmentHistoryEntry create(@NotNull String name, @Nullable Integer startYear, @Nullable Integer startMonth,
            @Nullable Integer stopYear, @Nullable Integer stopMonth) {
        return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(ImmutableOtherTreatment.builder().name(name).isSystemic(false).build())
                .startYear(startYear)
                .startMonth(startMonth)
                .treatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder().stopYear(stopYear).stopMonth(stopMonth).build())
                .build();
    }
}