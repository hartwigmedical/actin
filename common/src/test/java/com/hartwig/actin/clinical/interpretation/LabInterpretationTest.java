package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;
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
        LabInterpretation empty = LabInterpretation.fromMeasurements(ArrayListMultimap.create());

        assertNull(empty.mostRecentRelevantDate());

        assertNull(empty.mostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        assertNull(empty.secondMostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        assertNull(empty.allValues(LabMeasurement.ALANINE_AMINOTRANSFERASE));
    }

    @Test
    public void canInterpretLabValues() {
        Multimap<LabMeasurement, LabValue> measurements = ArrayListMultimap.create();
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(1)).build());
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(5)).build());
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(3)).build());
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(2)).build());
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(4)).build());
        measurements.put(LabMeasurement.ALBUMIN, builder().date(TEST_DATE.minusDays(4)).build());

        measurements.put(LabMeasurement.THROMBOCYTES_ABS, builder().date(TEST_DATE.minusDays(2)).build());
        measurements.put(LabMeasurement.THROMBOCYTES_ABS, builder().date(TEST_DATE.minusDays(3)).build());

        LabInterpretation interpretation = LabInterpretation.fromMeasurements(measurements);

        assertEquals(TEST_DATE.minusDays(1), interpretation.mostRecentRelevantDate());

        assertEquals(6, interpretation.allValues(LabMeasurement.ALBUMIN).size());
        assertEquals(TEST_DATE.minusDays(1), interpretation.mostRecentValue(LabMeasurement.ALBUMIN).date());
        assertEquals(TEST_DATE.minusDays(2), interpretation.secondMostRecentValue(LabMeasurement.ALBUMIN).date());

        assertEquals(2, interpretation.allValues(LabMeasurement.THROMBOCYTES_ABS).size());
        assertEquals(TEST_DATE.minusDays(2), interpretation.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS).date());
        assertEquals(TEST_DATE.minusDays(3), interpretation.secondMostRecentValue(LabMeasurement.THROMBOCYTES_ABS).date());

        assertNull(interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS));
        assertNull(interpretation.secondMostRecentValue(LabMeasurement.LEUKOCYTES_ABS));
        assertNull(interpretation.allValues(LabMeasurement.LEUKOCYTES_ABS));
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