package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCDatabaseEvaluatorTest {

    private val evaluator = CTCDatabaseEvaluator(TestCTCDatabaseFactory.createProperTestCTCDatabase())

    @Test
    fun `Should pick up unused study METCs to ignore`() {
        // The proper CTC database has no unused METCs to ignore
        assertThat(evaluator.extractUnusedStudyMETCsToIgnore()).isEmpty()
        evaluator.evaluateDatabaseConfiguration()

        val evaluatorWithUnused =
            CTCDatabaseEvaluator(
                TestCTCDatabaseFactory.createMinimalTestCTCDatabase().copy(studyMETCsToIgnore = setOf("unused")))
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
            CTCDatabaseEvaluator(TestCTCDatabaseFactory.createMinimalTestCTCDatabase().copy(unmappedCohortIds = setOf(1)))
        val unusedUnmappedCohortIds = evaluatorWithUnused.extractUnusedUnmappedCohorts()
        assertThat(unusedUnmappedCohortIds).containsExactly(1)

        evaluatorWithUnused.evaluateDatabaseConfiguration()
    }
}

