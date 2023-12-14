package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import org.junit.Assert
import org.junit.Test

class TrialMatchSummarizerTest {
    @Test
    fun canSummarizeTestData() {
        val summary = TrialMatchSummarizer.summarize(TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches())
        assertEquals(2, summary.trialCount())
        assertEquals(5, summary.cohortCount())
        assertEquals(2, summary.eligibleTrialMap().size())
        val eligibleCohorts: List<CohortMetadata> = summary.eligibleTrialMap().get(findByTrialId(summary, "Test Trial 1"))
        Assert.assertEquals(2, eligibleCohorts.size.toLong())
        Assert.assertNotNull(findByCohortId(eligibleCohorts, "A"))
        Assert.assertNotNull(findByCohortId(eligibleCohorts, "B"))
    }

    companion object {
        private fun findByTrialId(summary: TrialMatchSummary, trialIdToFind: String): TrialIdentification {
            for (identification in summary.eligibleTrialMap().keySet()) {
                if (identification.trialId() == trialIdToFind) {
                    return identification
                }
            }
            throw IllegalStateException("Could not find trial with id $trialIdToFind")
        }

        private fun findByCohortId(cohorts: List<CohortMetadata>, cohortIdToFind: String): CohortMetadata {
            for (cohort in cohorts) {
                if (cohort.cohortId() == cohortIdToFind) {
                    return cohort
                }
            }
            throw IllegalStateException("Could not find cohort with id: $cohortIdToFind")
        }
    }
}