package com.hartwig.actin.clinical.sort;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class MedicationComparatorTest {

    @Test
    public void canSortMedications() {
        ImmutableMedication.Builder builder = ImmutableMedication.builder().type(Strings.EMPTY);

        Medication medication1 = builder.name("X").build();
        Medication medication2 = builder.name("Z").build();
        Medication medication3 = builder.name("Y").build();
        List<Medication> values = Lists.newArrayList(medication1, medication2, medication3);

        values.sort(new MedicationComparator());

        assertEquals(medication1, values.get(0));
        assertEquals(medication3, values.get(1));
        assertEquals(medication2, values.get(2));
    }
}