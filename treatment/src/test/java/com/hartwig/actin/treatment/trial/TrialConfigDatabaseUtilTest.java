package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TrialConfigDatabaseUtilTest {

    @Test
    public void canConvertToReferenceIds() {
        assertEquals(1, TrialConfigDatabaseUtil.toReferenceIds("all").size());
        assertEquals(1, TrialConfigDatabaseUtil.toReferenceIds("I-01").size());

        Set<String> referenceIds = TrialConfigDatabaseUtil.toReferenceIds("I-01, I-02");
        assertEquals(2, referenceIds.size());
        assertTrue(referenceIds.contains("I-01"));
        assertTrue(referenceIds.contains("I-02"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnMissingReferenceIdsParam() {
        TrialConfigDatabaseUtil.toReferenceIds(Strings.EMPTY);
    }

    @Test
    public void canConvertToCohorts() {
        assertEquals(0, TrialConfigDatabaseUtil.toCohorts("all").size());
        assertEquals(1, TrialConfigDatabaseUtil.toCohorts("A").size());

        Set<String> cohorts = TrialConfigDatabaseUtil.toCohorts("A, B");
        assertEquals(2, cohorts.size());
        assertTrue(cohorts.contains("A"));
        assertTrue(cohorts.contains("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnMissingCohortParam() {
        TrialConfigDatabaseUtil.toCohorts(Strings.EMPTY);
    }

}