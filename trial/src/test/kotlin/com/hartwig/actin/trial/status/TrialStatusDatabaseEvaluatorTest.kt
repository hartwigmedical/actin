package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.status.config.TestCTCDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusDatabaseEvaluatorTest {

    private val evaluator = TrialStatusDatabaseEvaluator(TestCTCDatabaseFactory.createProperTestCTCDatabase())

    @Test
    fun shouldPickUpUnusedStudyMETCsToIgnore() {
        // The proper CTC database has no unused METCs to ignore
        assertThat(evaluator.extractUnusedStudyMETCsToIgnore()).isEmpty()
        evaluator.evaluateDatabaseConfiguration()

        val evaluatorWithUnused =
            TrialStatusDatabaseEvaluator(TestCTCDatabaseFactory.createMinimalTestCTCDatabase().copy(studyMETCsToIgnore = setOf("unused")))
        val unusedStudyMETCs = evaluatorWithUnused.extractUnusedStudyMETCsToIgnore()
        assertThat(unusedStudyMETCs).containsExactly("unused")

        evaluatorWithUnused.evaluateDatabaseConfiguration()
    }

    @Test
    fun shouldPickUpUnusedUnmappedCohortIds() {
        // The proper CTC database has no unused unmapped cohort IDs to ignore
        assertThat(evaluator.extractUnusedUnmappedCohorts()).isEmpty()
        evaluator.evaluateDatabaseConfiguration()

        val evaluatorWithUnused =
            TrialStatusDatabaseEvaluator(TestCTCDatabaseFactory.createMinimalTestCTCDatabase().copy(unmappedCohortIds = setOf(1)))
        val unusedUnmappedCohortIds = evaluatorWithUnused.extractUnusedUnmappedCohorts()
        assertThat(unusedUnmappedCohortIds).containsExactly(1)

        evaluatorWithUnused.evaluateDatabaseConfiguration()
    }
}

