package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
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
    fun shouldIndicateDriverIsActionableIfEventMatchesOpenTrial() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        assertThat(createInterpreter().driverIsActionable(driverForEvent(ELIGIBLE_EVENT))).isTrue
    }

    @Test
    fun shouldIndicateDriverIsActionableIfExternalTrialsExist() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.createMinimal().copy(
            event = CLOSED_COHORT,
            evidence = TestClinicalEvidenceFactory.withExternalEligibleTrial(TestExternalTrialFactory.createTestTrial())
        )
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    @Test
    fun shouldIndicateDriverIsActionableIfApprovedTreatmentsExist() {
        assertThat(createInterpreter().driverIsActionable(driverForEvent(CLOSED_COHORT))).isFalse
        val driver: Driver = TestVariantFactory.createMinimal().copy(
            event = CLOSED_COHORT,
            evidence = TestClinicalEvidenceFactory.withApprovedTreatment("treatment")
        )
        assertThat(createInterpreter().driverIsActionable(driver)).isTrue
    }

    companion object {
        private const val INELIGIBLE_COHORT = "INELIGIBLE"
        private const val CLOSED_COHORT = "CLOSED"
        private const val ELIGIBLE_COHORT = "ELIGIBLE"
        private const val ELIGIBLE_COHORT_2 = "ELIGIBLE2"
        private const val ELIGIBLE_EVENT = "event"
        private fun driverForEvent(event: String): Driver {
            return TestVariantFactory.createMinimal().copy(event = event)
        }

        private fun evaluatedCohort(name: String, isEligible: Boolean, isOpen: Boolean, event: String = name): EvaluatedCohort {
            return EvaluatedCohortTestFactory.evaluatedCohort(
                acronym = name,
                isPotentiallyEligible = isEligible,
                isOpen = isOpen,
                molecularEvents = setOf(event)
            )
        }

        private fun createInterpreter(): EvaluatedCohortsInterpreter {
            return EvaluatedCohortsInterpreter.fromEvaluatedCohorts(
                listOf(
                    evaluatedCohort(INELIGIBLE_COHORT, false, true),
                    evaluatedCohort(CLOSED_COHORT, true, false),
                    evaluatedCohort(ELIGIBLE_COHORT, true, true, ELIGIBLE_EVENT),
                    evaluatedCohort(ELIGIBLE_COHORT_2, true, true, ELIGIBLE_EVENT)
                )
            )
        }
    }
}