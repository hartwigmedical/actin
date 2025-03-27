package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.TestServeFactory.createServeDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ServeCleanerTest {
    @Test
    fun `Should remove evidences with combined criteria`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = COMBINED_PROFILE)
        val database = createServeDatabase(evidence, TestServeTrialFactory.createTrialForGene())
        val cleanDatabase = ServeCleaner.cleanServeDatabase(database)
        cleanDatabase.records().values.forEach { assertThat(it.evidences()).isEmpty() }
    }

    @Test
    fun `Should retain evidences with simple criteria`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val database = createServeDatabase(evidence, TestServeTrialFactory.createTrialForGene())
        val cleanDatabase = ServeCleaner.cleanServeDatabase(database)
        cleanDatabase.records().values.forEach { assertThat(it.evidences()).containsExactly(evidence) }
    }
}