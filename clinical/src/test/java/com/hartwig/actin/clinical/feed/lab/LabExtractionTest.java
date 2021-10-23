package com.hartwig.actin.clinical.feed.lab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.feed.TestFeedFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class LabExtractionTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractLabValues() {
        List<LabEntry> testEntries = TestFeedFactory.createTestLabEntries();

        LabValue lab1 = LabExtraction.extract(findByCodeCodeOriginal(testEntries, "LAB1"));
        assertEquals(LocalDate.of(2018, 5, 29), lab1.date());
        assertEquals(Strings.EMPTY, lab1.comparator());
        assertEquals(30, lab1.value(), EPSILON);
        assertEquals("U/l", lab1.unit());
        assertEquals(20, lab1.refLimitLow(), EPSILON);
        assertEquals(40, lab1.refLimitUp(), EPSILON);
        assertFalse(lab1.isOutsideRef());

        LabValue lab2 = LabExtraction.extract(findByCodeCodeOriginal(testEntries, "LAB2"));
        assertEquals(LocalDate.of(2018, 5, 29), lab2.date());
        assertEquals(Strings.EMPTY, lab2.comparator());
        assertEquals(22, lab2.value(), EPSILON);
        assertEquals("mmol/l", lab2.unit());
        assertEquals(30, lab2.refLimitLow(), EPSILON);
        assertNull(lab2.refLimitUp());
        assertTrue(lab2.isOutsideRef());

        LabValue lab3 = LabExtraction.extract(findByCodeCodeOriginal(testEntries, "LAB3"));
        assertEquals(LocalDate.of(2018, 5, 29), lab3.date());
        assertEquals(">", lab3.comparator());
        assertEquals(50, lab3.value(), EPSILON);
        assertEquals("mL/min", lab3.unit());
        assertEquals(50, lab3.refLimitLow(), EPSILON);
        assertNull(lab3.refLimitUp());
        assertFalse(lab3.isOutsideRef());

        LabValue lab4 = LabExtraction.extract(findByCodeCodeOriginal(testEntries, "LAB4"));
        assertNull(lab4.refLimitLow());
        assertNull(lab4.refLimitUp());
        assertNull(lab4.isOutsideRef());
    }

    @NotNull
    private static LabEntry findByCodeCodeOriginal(@NotNull List<LabEntry> entries, @NotNull String code) {
        for (LabEntry entry : entries) {
            if (entry.codeCodeOriginal().equals(code)) {
                return entry;
            }
        }

        throw new IllegalStateException("Could not find lab entry with code: " + code);
    }

    @Test
    public void canExtractLimits() {
        LabExtraction.Limits bothPositive = LabExtraction.extractLimits("12 - 14");
        assertEquals(12, bothPositive.lower(), EPSILON);
        assertEquals(14, bothPositive.upper(), EPSILON);

        LabExtraction.Limits bothOneNegative = LabExtraction.extractLimits("-3 - 3");
        assertEquals(-3, bothOneNegative.lower(), EPSILON);
        assertEquals(3, bothOneNegative.upper(), EPSILON);

        LabExtraction.Limits bothTwoNegative = LabExtraction.extractLimits("-6 - -3");
        assertEquals(-6, bothTwoNegative.lower(), EPSILON);
        assertEquals(-3, bothTwoNegative.upper(), EPSILON);

        LabExtraction.Limits lowerOnlyPositive = LabExtraction.extractLimits("> 50");
        assertEquals(50, lowerOnlyPositive.lower(), EPSILON);
        assertNull(lowerOnlyPositive.upper());

        LabExtraction.Limits lowerOnlyNegative = LabExtraction.extractLimits("> -6");
        assertEquals(-6, lowerOnlyNegative.lower(), EPSILON);
        assertNull(lowerOnlyNegative.upper());

        LabExtraction.Limits upperOnly = LabExtraction.extractLimits("<90");
        assertNull(upperOnly.lower());
        assertEquals(90, upperOnly.upper(), EPSILON);

        LabExtraction.Limits failed = LabExtraction.extractLimits("not a limit");
        assertNull(failed.lower());
        assertNull(failed.upper());
    }

    @Test
    public void canFindSeparatingHyphen() {
        assertEquals(2, LabExtraction.findSeparatingHyphenIndex("3 - 5"));
        assertEquals(4, LabExtraction.findSeparatingHyphenIndex("3,1 - 5,1"));
        assertEquals(2, LabExtraction.findSeparatingHyphenIndex("-3-5"));
        assertEquals(2, LabExtraction.findSeparatingHyphenIndex("-3--5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashFindSeparatingHyphenOnOnInvalidReferenceRangeText() {
        assertEquals(1, LabExtraction.findSeparatingHyphenIndex("-Nope"));
    }
}