package com.hartwig.actin.treatment.trial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TrialConfigDatabaseUtilTest {

    @Test
    public void canConvertToCriterionIds() {
        assertEquals(1, TrialConfigDatabaseUtil.toCriterionIds("all").size());
        assertEquals(1, TrialConfigDatabaseUtil.toCriterionIds("I-01").size());

        Set<String> criterionIds = TrialConfigDatabaseUtil.toCriterionIds("I-01; I-02");
        assertEquals(2, criterionIds.size());
        assertTrue(criterionIds.contains("I-01"));
        assertTrue(criterionIds.contains("I-02"));
    }

    @Test
    public void canConvertToCohorts() {
        assertEquals(0, TrialConfigDatabaseUtil.toCohorts("all").size());
        assertEquals(1, TrialConfigDatabaseUtil.toCohorts("A").size());

        Set<String> cohorts = TrialConfigDatabaseUtil.toCohorts("A; B");
        assertEquals(2, cohorts.size());
        assertTrue(cohorts.contains("A"));
        assertTrue(cohorts.contains("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnMissingCohortParam() {
        TrialConfigDatabaseUtil.toCohorts(Strings.EMPTY);
    }

    @Test
    public void canConvertToParams() {
        assertEquals(0, TrialConfigDatabaseUtil.toParameters(Strings.EMPTY).size());
        assertEquals(1, TrialConfigDatabaseUtil.toParameters("param1").size());
        assertEquals(2, TrialConfigDatabaseUtil.toParameters("param1;4").size());
    }
}