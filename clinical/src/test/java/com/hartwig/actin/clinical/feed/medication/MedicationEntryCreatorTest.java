package com.hartwig.actin.clinical.feed.medication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class MedicationEntryCreatorTest {

    @Test
    public void canInterpretActiveField() {
        assertNull(MedicationEntryCreator.isActive("NULL"));
        assertTrue(MedicationEntryCreator.isActive("active"));
        assertFalse(MedicationEntryCreator.isActive(Strings.EMPTY));
    }
}