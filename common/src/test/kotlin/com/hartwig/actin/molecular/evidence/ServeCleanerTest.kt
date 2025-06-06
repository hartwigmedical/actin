package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.TestServeFactory.createServeDatabase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ServeCleanerTest {

    @Test
    fun `Should remove combined criteria from trials also containing single criteria`() {
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(COMBINED_PROFILE, SINGLE_PROFILE_1))
        val evidence = TestServeEvidenceFactory.create()
        val database = createServeDatabase(evidence, trial)

        val cleanDatabase = ServeCleaner.cleanServeDatabase(database)

        cleanDatabase.records().values.forEach { record ->
            assertThat(record.trials().size).isEqualTo(1)
            assertThat(record.trials().first().anyMolecularCriteria()).containsExactly(SINGLE_PROFILE_1)
        }
    }

    @Test
    fun `Should completely remove trials containing only combined criteria`() {
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(COMBINED_PROFILE))
        val evidence = TestServeEvidenceFactory.create()
        val database = createServeDatabase(evidence, trial)

        val cleanDatabase = ServeCleaner.cleanServeDatabase(database)

        cleanDatabase.records().values.forEach { record ->
            assertThat(record.trials()).isEmpty()
        }
    }
}