package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.trial.CohortMetadata
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialMatchSummarizerTest {

    @Test
    fun `Should be able to summarize proper treatment match test data`() {
        val summary = TrialMatchSummarizer.summarize(TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches)
        assertThat(summary.trialCount).isEqualTo(2)
        assertThat(summary.cohortCount).isEqualTo(4)
        assertThat(summary.eligibleTrialMap).hasSize(2)

        val eligibleCohorts = summary.eligibleTrialMap.entries.first { (key, _) -> key.nctId == "NCT00000010" }.value
        assertThat(eligibleCohorts.map(CohortMetadata::cohortId)).containsExactlyInAnyOrder("A", "B")
    }
}