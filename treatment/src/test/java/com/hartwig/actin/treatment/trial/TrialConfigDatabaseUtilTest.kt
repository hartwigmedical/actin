package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil.toCohorts
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil.toReferenceIds
import org.apache.logging.log4j.util.Strings
import org.junit.Assert
import org.junit.Test

class TrialConfigDatabaseUtilTest {
    @Test
    fun canConvertToReferenceIds() {
        Assert.assertEquals(1, toReferenceIds("all").size.toLong())
        Assert.assertEquals(1, toReferenceIds("I-01").size.toLong())
        Assert.assertTrue(toReferenceIds(Strings.EMPTY).isEmpty())
        val referenceIds = toReferenceIds("I-01, I-02")
        Assert.assertEquals(2, referenceIds.size.toLong())
        Assert.assertTrue(referenceIds.contains("I-01"))
        Assert.assertTrue(referenceIds.contains("I-02"))
    }

    @Test
    fun canConvertToCohorts() {
        Assert.assertEquals(0, toCohorts("all").size.toLong())
        Assert.assertEquals(1, toCohorts("A").size.toLong())
        val cohorts = toCohorts("A, B")
        Assert.assertEquals(2, cohorts.size.toLong())
        Assert.assertTrue(cohorts.contains("A"))
        Assert.assertTrue(cohorts.contains("B"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnMissingCohortParam() {
        toCohorts(Strings.EMPTY)
    }
}