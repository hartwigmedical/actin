package com.hartwig.actin.treatment.database;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class TreatmentDatabaseUtilTest {

    @Test
    public void canConvertToCohorts() {
        assertEquals(0, TreatmentDatabaseUtil.toCohorts("all").size());
        assertEquals(1, TreatmentDatabaseUtil.toCohorts("A").size());
        assertEquals(2, TreatmentDatabaseUtil.toCohorts("A;B").size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnMissingCohortParam() {
        TreatmentDatabaseUtil.toCohorts(Strings.EMPTY);
    }

    @Test
    public void canConvertToParams() {
        assertEquals(0, TreatmentDatabaseUtil.toParameters(Strings.EMPTY).size());
        assertEquals(1, TreatmentDatabaseUtil.toParameters("param1").size());
        assertEquals(2, TreatmentDatabaseUtil.toParameters("param1;4").size());
    }
}