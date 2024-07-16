package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusDatabaseEvaluatorTest {

    private val evaluator = TrialStatusDatabaseEvaluator(TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase())

    @Test
    fun `Should pick up unused study METCs to ignore`() {
        // The proper CTC database has no unused METCs to ignore
        assertThat(evaluator.extractUnusedStudyMETCsToIgnore()).isEmpty()
        evaluator.evaluateDatabaseConfiguration()

        val evaluatorWithUnused =
            TrialStatusDatabaseEvaluator(
                TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase().copy(studyMETCsToIgnore = setOf("unused"))
            )
        val unusedStudyMETCs = evaluatorWithUnused.extractUnusedStudyMETCsToIgnore()
        assertThat(unusedStudyMETCs).containsExactly("unused")

        evaluatorWithUnused.evaluateDatabaseConfiguration()
    }

    @Test
    fun `Should pick up unused unmapped cohort IDs`() {
        // The proper CTC database has no unused unmapped cohort IDs to ignore
        assertThat(evaluator.extractUnusedUnmappedCohorts()).isEmpty()
        evaluator.evaluateDatabaseConfiguration()

        val evaluatorWithUnused =
            TrialStatusDatabaseEvaluator(
                TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase().copy(unmappedCohortIds = setOf("1"))
            )
        val unusedUnmappedCohortIds = evaluatorWithUnused.extractUnusedUnmappedCohorts()
        assertThat(unusedUnmappedCohortIds).containsExactly("1")

        evaluatorWithUnused.evaluateDatabaseConfiguration()
    }
}

