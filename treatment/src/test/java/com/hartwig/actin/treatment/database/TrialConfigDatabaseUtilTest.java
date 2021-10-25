package com.hartwig.actin.treatment.database;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TrialConfigDatabaseUtilTest {

    @Test
    public void canConvertToCohorts() {
        assertEquals(0, TrialConfigDatabaseUtil.toCohorts("all").size());
        assertEquals(1, TrialConfigDatabaseUtil.toCohorts("A").size());
        assertEquals(2, TrialConfigDatabaseUtil.toCohorts("A;B").size());
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