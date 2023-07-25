package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class TreatmentHistoryAscendingDateComparatorFactoryTest {

    @Test
    public void shouldSortByAscendingStartDateThenByAscendingEndDateThenByName() {
        TreatmentHistoryEntry treatment1 = create("treatment A", null, null, null, null);
        TreatmentHistoryEntry treatment2 = create("treatment A", 2018, null, null, null);
        TreatmentHistoryEntry treatment3 = create("treatment A", 2020, 2, null, null);
        TreatmentHistoryEntry treatment4 = create("treatment A", 2020, 3, null, null);
        TreatmentHistoryEntry treatment5 = create("treatment A", 2020, 3, 2021, null);
        TreatmentHistoryEntry treatment6 = create("treatment A", 2020, 3, 2021, 6);
        TreatmentHistoryEntry treatment7 = create("treatment B", 2020, 3, 2021, 6);
        TreatmentHistoryEntry treatment8 = create("treatment A", 2021, 1, null, null);

        List<TreatmentHistoryEntry> treatments =
                Lists.newArrayList(treatment8, treatment6, treatment3, treatment7, treatment4, treatment1, treatment5, treatment2);
        treatments.sort(TreatmentHistoryAscendingDateComparatorFactory.treatmentHistoryEntryComparator());

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
    private static TreatmentHistoryEntry create(@NotNull String name, @Nullable Integer startYear, @Nullable Integer startMonth,
            @Nullable Integer stopYear, @Nullable Integer stopMonth) {
        return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(ImmutableOtherTreatment.builder().name(name).isSystemic(false).build())
                .startYear(startYear)
                .startMonth(startMonth)
                .therapyHistoryDetails(ImmutableTherapyHistoryDetails.builder().stopYear(stopYear).stopMonth(stopMonth).build())
                .build();
    }
}