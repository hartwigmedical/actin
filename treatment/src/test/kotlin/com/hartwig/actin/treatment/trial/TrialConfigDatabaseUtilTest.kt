package com.hartwig.actin.treatment.trial

import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil.toCohorts
import com.hartwig.actin.treatment.trial.TrialConfigDatabaseUtil.toReferenceIds
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseUtilTest {
    @Test
    fun canConvertToReferenceIds() {
        assertThat(toReferenceIds("all")).hasSize(1)
        assertThat(toReferenceIds("I-01")).hasSize(1)
        assertThat(toReferenceIds(Strings.EMPTY)).isEmpty()

        val referenceIds = toReferenceIds("I-01, I-02")
        assertThat(referenceIds).containsExactlyInAnyOrder("I-01", "I-02")
    }

    @Test
    fun canConvertToCohorts() {
        assertThat(toCohorts("all")).hasSize(0)
        assertThat(toCohorts("A")).hasSize(1)

        val cohorts = toCohorts("A, B")
        assertThat(cohorts).containsExactlyInAnyOrder("A", "B")
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnMissingCohortParam() {
        toCohorts(Strings.EMPTY)
    }
}