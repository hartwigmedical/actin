package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvaluatedCohortsInterpreterTest {
    @Test
    fun shouldReturnAllEligibleAndOpenCohortsForDriver() {
        val matchingTrials = createInterpreter().trialsForDriver(driverForEvent(ELIGIBLE_EVENT))
        assertThat(matchingTrials).containsExactlyInAnyOrder(ELIGIBLE_COHORT, ELIGIBLE_COHORT_2)
    }

    @Test
    fun shouldNotReturnMatchesForIneligibleCohorts() {
        assertThat(createInterpreter().trialsForDriver(driverForEvent(INELIGIBLE_COHORT))).isEmpty()
    }

    @Test
    fun shouldNotReturnMatchesForClosedCohorts() {
        assertThat(createInterpreter().trialsForDriver(driverForEvent(CLOSED_COHORT))).isEmpty()
    }

    @Test
    fun shouldIndicateDriverIsActionableIfEventMatchesNonBlacklistedTrial() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(BLACKLISTED_COHORT))).isFalse
        assertThat(createInterpreter().driverIsActionable(driverForEvent(ELIGIBLE_EVENT))).isTrue
    }

    @Test
    fun shouldIndicateDriverIsActionableIfExternalTrialsNonBlacklisted() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(BLACKLISTED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.builder()
            .event(BLACKLISTED_COHORT)
            .evidence(TestActionableEvidenceFactory.withExternalEligibleTrial("external"))
            .build()
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    @Test
    fun shouldIndicateDriverIsActionableIfApprovedTreatmentsExist() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(BLACKLISTED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.builder()
            .event(BLACKLISTED_COHORT)
            .evidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
            .build()
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    companion object {
        private const val BLACKLISTED_COHORT = "BLACKLIST"
        private const val INELIGIBLE_COHORT = "INELIGIBLE"
        private const val CLOSED_COHORT = "CLOSED"
        private const val ELIGIBLE_COHORT = "ELIGIBLE"
        private const val ELIGIBLE_COHORT_2 = "ELIGIBLE2"
        private const val ELIGIBLE_EVENT = "event"
        private fun driverForEvent(event: String): Driver {
            return TestVariantFactory.builder().event(event).build()
        }

        private fun evaluatedCohort(
            name: String,
            isEligible: Boolean,
            isBlacklisted: Boolean,
            isOpen: Boolean,
            event: String = name
        ): EvaluatedCohort {
            return EvaluatedCohortTestFactory.evaluatedCohort(
                acronym = name,
                isPotentiallyEligible = isEligible,
                isBlacklisted = isBlacklisted,
                isOpen = isOpen,
                molecularEvents = setOf(event)
            )
        }

        private fun createInterpreter(): EvaluatedCohortsInterpreter {
            return EvaluatedCohortsInterpreter(
                listOf(
                    evaluatedCohort(BLACKLISTED_COHORT, false, true, true),
                    evaluatedCohort(INELIGIBLE_COHORT, false, false, true),
                    evaluatedCohort(CLOSED_COHORT, true, false, false),
                    evaluatedCohort(ELIGIBLE_COHORT, true, false, true, ELIGIBLE_EVENT),
                    evaluatedCohort(ELIGIBLE_COHORT_2, true, false, true, ELIGIBLE_EVENT)
                )
            )
        }
    }
}