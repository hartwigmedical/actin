package com.hartwig.actin.trial.trial

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialConfigDatabaseUtilTest {

    @Test
    fun canConvertToReferenceIds() {
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("all")).hasSize(1)
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("I-01")).hasSize(1)
        assertThat(TrialConfigDatabaseUtil.toReferenceIds("")).isEmpty()

        val referenceIds = TrialConfigDatabaseUtil.toReferenceIds("I-01, I-02")
        assertThat(referenceIds).containsExactlyInAnyOrder("I-01", "I-02")
    }

    @Test
    fun canConvertToCohorts() {
        assertThat(TrialConfigDatabaseUtil.toCohorts("all")).hasSize(0)
        assertThat(TrialConfigDatabaseUtil.toCohorts("A")).hasSize(1)

        val cohorts = TrialConfigDatabaseUtil.toCohorts("A, B")
        assertThat(cohorts).containsExactlyInAnyOrder("A", "B")
    }

    @Test(expected = IllegalArgumentException::class)
    fun crashOnMissingCohortParam() {
        TrialConfigDatabaseUtil.toCohorts("")
    }
}