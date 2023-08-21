package com.hartwig.actin.report.interpretation

import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import org.junit.Assert
import org.junit.Test
import java.util.List

class EvaluatedCohortsInterpreterTest {
    @Test
    fun shouldReturnAllEligibleAndOpenCohortsForDriver() {
        val matchingTrials = createInterpreter().trialsForDriver(driverForEvent(ELIGIBLE_EVENT))
        Assert.assertEquals(2, matchingTrials.size.toLong())
        Assert.assertTrue(matchingTrials.contains(ELIGIBLE_COHORT))
        Assert.assertTrue(matchingTrials.contains(ELIGIBLE_COHORT_2))
    }

    @Test
    fun shouldNotReturnMatchesForIneligibleCohorts() {
        Assert.assertTrue(createInterpreter().trialsForDriver(driverForEvent(INELIGIBLE_COHORT)).isEmpty())
    }

    @Test
    fun shouldNotReturnMatchesForClosedCohorts() {
        Assert.assertTrue(createInterpreter().trialsForDriver(driverForEvent(CLOSED_COHORT)).isEmpty())
    }

    @Test
    fun shouldIndicateDriverIsActionableIfEventMatchesEligibleTrial() {
        Assert.assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)))
        Assert.assertTrue(createInterpreter().driverIsActionable(driverForEvent(ELIGIBLE_EVENT)))
    }

    @Test
    fun shouldIndicateDriverIsActionableIfExternalTrialsEligible() {
        Assert.assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)))
        val driver: Driver = TestVariantFactory.builder()
            .event(INELIGIBLE_COHORT)
            .evidence(TestActionableEvidenceFactory.withExternalEligibleTrial("external"))
            .build()
        Assert.assertTrue(createInterpreter().driverIsActionable(driver))
    }

    @Test
    fun shouldIndicateDriverIsActionableIfApprovedTreatmentsExist() {
        Assert.assertFalse(createInterpreter().driverIsActionable(driverForEvent(INELIGIBLE_COHORT)))
        val driver: Driver = TestVariantFactory.builder()
            .event(INELIGIBLE_COHORT)
            .evidence(TestActionableEvidenceFactory.withApprovedTreatment("treatment"))
            .build()
        Assert.assertTrue(createInterpreter().driverIsActionable(driver))
    }

    companion object {
        private const val INELIGIBLE_COHORT = "INELIGIBLE"
        private const val CLOSED_COHORT = "CLOSED"
        private const val ELIGIBLE_COHORT = "ELIGIBLE"
        private const val ELIGIBLE_COHORT_2 = "ELIGIBLE2"
        private const val ELIGIBLE_EVENT = "event"
        private fun driverForEvent(event: String): Driver {
            return TestVariantFactory.builder().event(event).build()
        }

        private fun evaluatedCohort(name: String, isEligible: Boolean, isOpen: Boolean, event: String = name): EvaluatedCohort {
            return EvaluatedCohortTestFactory.builder()
                .acronym(name)
                .isPotentiallyEligible(isEligible)
                .isOpen(isOpen)
                .addMolecularEvents(event)
                .build()
        }

        private fun createInterpreter(): EvaluatedCohortsInterpreter {
            return EvaluatedCohortsInterpreter(
                List.of(
                    evaluatedCohort(INELIGIBLE_COHORT, false, true),
                    evaluatedCohort(CLOSED_COHORT, true, false),
                    evaluatedCohort(ELIGIBLE_COHORT, true, true, ELIGIBLE_EVENT),
                    evaluatedCohort(ELIGIBLE_COHORT_2, true, true, ELIGIBLE_EVENT)
                )
            )
        }
    }
}