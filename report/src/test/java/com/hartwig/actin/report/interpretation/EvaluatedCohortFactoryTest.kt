package com.hartwig.actin.report.interpretation

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.ImmutableTrialMatch
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory.create
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification
import org.junit.Assert
import org.junit.Test

class EvaluatedCohortFactoryTest {
    @Test
    fun canCreateEvaluatedCohortsFromMinimalMatch() {
        val cohorts = create(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
        Assert.assertTrue(cohorts.isEmpty())
    }

    @Test
    fun canCreateEvaluatedCohortsFromProperMatch() {
        val cohorts = create(TestTreatmentMatchFactory.createProperTreatmentMatch())
        Assert.assertEquals(5, cohorts.size.toLong())
        val trial1cohortA = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort A")
        Assert.assertFalse(trial1cohortA.molecularEvents().isEmpty())
        Assert.assertTrue(trial1cohortA.molecularEvents().contains("BRAF V600E"))
        Assert.assertTrue(trial1cohortA.isPotentiallyEligible)
        Assert.assertTrue(trial1cohortA.isOpen)
        Assert.assertFalse(trial1cohortA.hasSlotsAvailable())
        Assert.assertFalse(trial1cohortA.warnings().isEmpty())
        Assert.assertTrue(trial1cohortA.fails().isEmpty())
        val trial1cohortB = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort B")
        Assert.assertTrue(trial1cohortB.molecularEvents().isEmpty())
        Assert.assertTrue(trial1cohortB.isPotentiallyEligible)
        Assert.assertTrue(trial1cohortB.isOpen)
        Assert.assertTrue(trial1cohortB.hasSlotsAvailable())
        Assert.assertFalse(trial1cohortB.warnings().isEmpty())
        Assert.assertTrue(trial1cohortB.fails().isEmpty())
        val trial1cohortC = findByAcronymAndCohort(cohorts, "TEST-1", "Cohort C")
        Assert.assertTrue(trial1cohortC.molecularEvents().isEmpty())
        Assert.assertFalse(trial1cohortC.isPotentiallyEligible)
        Assert.assertFalse(trial1cohortC.isOpen)
        Assert.assertFalse(trial1cohortC.hasSlotsAvailable())
        Assert.assertFalse(trial1cohortC.warnings().isEmpty())
        Assert.assertFalse(trial1cohortC.fails().isEmpty())
        val trial2cohortA = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort A")
        Assert.assertFalse(trial2cohortA.molecularEvents().isEmpty())
        Assert.assertTrue(trial2cohortA.molecularEvents().contains("BRAF V600E"))
        Assert.assertTrue(trial2cohortA.isPotentiallyEligible)
        Assert.assertTrue(trial2cohortA.isOpen)
        Assert.assertFalse(trial2cohortA.hasSlotsAvailable())
        Assert.assertTrue(trial2cohortA.warnings().isEmpty())
        Assert.assertTrue(trial2cohortA.fails().isEmpty())
        val trial2cohortB = findByAcronymAndCohort(cohorts, "TEST-2", "Cohort B")
        Assert.assertTrue(trial2cohortB.molecularEvents().isEmpty())
        Assert.assertFalse(trial2cohortB.isPotentiallyEligible)
        Assert.assertTrue(trial2cohortB.isOpen)
        Assert.assertTrue(trial2cohortB.hasSlotsAvailable())
        Assert.assertTrue(trial2cohortB.warnings().isEmpty())
        Assert.assertFalse(trial2cohortB.fails().isEmpty())
    }

    @Test
    fun canEvaluateTrialsWithoutCohort() {
        val trialMatchWithoutCohort: TrialMatch = ImmutableTrialMatch.builder()
            .identification(
                ImmutableTrialIdentification.builder()
                    .trialId("test")
                    .open(true)
                    .acronym("test-1")
                    .title("Example test trial 1")
                    .build()
            )
            .isPotentiallyEligible(true)
            .build()
        val treatmentMatch: TreatmentMatch = ImmutableTreatmentMatch.builder()
            .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
            .addTrialMatches(trialMatchWithoutCohort)
            .build()
        val cohorts = create(treatmentMatch)
        Assert.assertEquals(1, cohorts.size.toLong())
    }

    companion object {
        private fun findByAcronymAndCohort(
            evaluatedCohorts: List<EvaluatedCohort>, acronymToFind: String,
            cohortToFind: String?
        ): EvaluatedCohort {
            for (evaluatedCohort in evaluatedCohorts) {
                if (evaluatedCohort.acronym() == acronymToFind && evaluatedCohort.cohort() == cohortToFind) {
                    return evaluatedCohort
                }
            }
            throw IllegalStateException("Could not find trial acronym $acronymToFind and cohort: $cohortToFind")
        }
    }
}