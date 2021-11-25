package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabInterpretationTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2020, 1, 1);

    @Test
    public void canDealWithMissingLabValues() {
        LabInterpretation interpretation = new LabInterpretation(ArrayListMultimap.create());

        assertNull(interpretation.mostRecentRelevantDate());
        assertNull(interpretation.mostRecentValue(LabMeasurement.ALAT));
    }

    @Test
    public void canInterpretLabValues() {
        LabInterpretation interpretation = createTestLabInterpretation(LabMeasurement.ALAT);

        assertEquals(TEST_DATE, interpretation.mostRecentRelevantDate());
        LabValue value = (interpretation.mostRecentValue(LabMeasurement.ALAT));
        assertNotNull(value);
        assertEquals(2, interpretation.allValuesForType(value).size());

        assertNull(interpretation.mostRecentValue(LabMeasurement.ALBUMIN));
        assertNull(interpretation.allValuesForType(builder().build()));
    }

    @NotNull
    private static LabInterpretation createTestLabInterpretation(@NotNull LabMeasurement measurement) {
        Multimap<LabMeasurement, LabValue> labValuesMap = ArrayListMultimap.create();
        labValuesMap.put(measurement, builder().code(measurement.code()).build());
        labValuesMap.put(measurement, builder().code(measurement.code()).build());

        return new LabInterpretation(labValuesMap);
    }

    @NotNull
    private static ImmutableLabValue.Builder builder() {
        return ImmutableLabValue.builder()
                .date(TEST_DATE)
                .code(Strings.EMPTY)
                .name(Strings.EMPTY)
                .comparator(Strings.EMPTY)
                .value(0D)
                .unit(Strings.EMPTY);
    }
}