package com.hartwig.actin.clinical.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.junit.Test;

public class LabInterpretationTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2020, 1, 1);

    @Test
    public void canDealWithMissingLabValues() {
        LabInterpretation empty = LabInterpretation.fromMeasurements(ArrayListMultimap.create());

        assertNull(empty.mostRecentRelevantDate());
        assertTrue(empty.allDates().isEmpty());

        assertNull(empty.mostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        assertNull(empty.secondMostRecentValue(LabMeasurement.ALANINE_AMINOTRANSFERASE));
        assertNull(empty.allValues(LabMeasurement.ALANINE_AMINOTRANSFERASE));

        assertTrue(empty.valuesOnDate(LabMeasurement.ALANINE_AMINOTRANSFERASE, TEST_DATE).isEmpty());
    }

    @Test
    public void canInterpretLabValues() {
        Multimap<LabMeasurement, LabValue> measurements = ArrayListMultimap.create();
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(1)).build());
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(5)).build());
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(3)).build());
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(2)).build());
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(4)).build());
        measurements.put(LabMeasurement.ALBUMIN, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(4)).build());

        measurements.put(LabMeasurement.THROMBOCYTES_ABS, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(2)).build());
        measurements.put(LabMeasurement.THROMBOCYTES_ABS, LabInterpretationTestUtil.builder().date(TEST_DATE.minusDays(3)).build());

        LabInterpretation interpretation = LabInterpretation.fromMeasurements(measurements);

        LocalDate mostRecent = interpretation.mostRecentRelevantDate();
        assertEquals(TEST_DATE.minusDays(1), mostRecent);

        Set<LocalDate> allDates = interpretation.allDates();
        assertEquals(5, allDates.size());
        assertEquals(mostRecent, allDates.iterator().next());

        assertEquals(6, interpretation.allValues(LabMeasurement.ALBUMIN).size());
        assertEquals(TEST_DATE.minusDays(1), interpretation.mostRecentValue(LabMeasurement.ALBUMIN).date());
        assertEquals(TEST_DATE.minusDays(2), interpretation.secondMostRecentValue(LabMeasurement.ALBUMIN).date());
        assertEquals(1, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(3)).size());
        assertEquals(2, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(4)).size());
        assertEquals(0, interpretation.valuesOnDate(LabMeasurement.ALBUMIN, TEST_DATE.minusDays(6)).size());

        assertEquals(2, interpretation.allValues(LabMeasurement.THROMBOCYTES_ABS).size());
        assertEquals(TEST_DATE.minusDays(2), interpretation.mostRecentValue(LabMeasurement.THROMBOCYTES_ABS).date());
        assertEquals(TEST_DATE.minusDays(3), interpretation.secondMostRecentValue(LabMeasurement.THROMBOCYTES_ABS).date());

        assertNull(interpretation.mostRecentValue(LabMeasurement.LEUKOCYTES_ABS));
        assertNull(interpretation.secondMostRecentValue(LabMeasurement.LEUKOCYTES_ABS));
        assertNull(interpretation.allValues(LabMeasurement.LEUKOCYTES_ABS));
        assertTrue(interpretation.valuesOnDate(LabMeasurement.LEUKOCYTES_ABS, mostRecent).isEmpty());
    }
}