package com.hartwig.actin.treatment.serialization

import com.google.common.io.Resources
import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TestTrialFactory
import com.hartwig.actin.treatment.datamodel.Trial
import com.hartwig.actin.treatment.serialization.TrialJson.fromJson
import com.hartwig.actin.treatment.serialization.TrialJson.readFromDir
import com.hartwig.actin.treatment.serialization.TrialJson.toJson
import org.junit.Assert
import org.junit.Test

class TrialJsonTest {
    @Test
    fun canConvertBackAndForthJson() {
        val minimal = TestTrialFactory.createMinimalTestTrial()
        val convertedMinimal = fromJson(toJson(minimal))
        Assert.assertEquals(minimal, convertedMinimal)
        val proper = TestTrialFactory.createProperTestTrial()
        val convertedProper = fromJson(toJson(proper))
        Assert.assertEquals(proper, convertedProper)
    }

    @Test
    fun canReadTreatmentDirectory() {
        val trials = readFromDir(TREATMENT_DIRECTORY)
        Assert.assertEquals(1, trials.size.toLong())
        assertTrial(trials[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotReadFilesFromNonDir() {
        readFromDir(TREATMENT_DIRECTORY + "/file.json")
    }

    companion object {
        private val TREATMENT_DIRECTORY = Resources.getResource("treatment").path
        private fun assertTrial(trial: Trial) {
            assertEquals("test trial", trial.identification().trialId())
            assertEquals("TEST-TRIAL", trial.identification().acronym())
            assertEquals("This is a trial to test ACTIN", trial.identification().title())
            assertEquals(1, trial.generalEligibility().size())
            val generalFunction = findBaseFunction(trial.generalEligibility(), EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
            assertEquals(1, generalFunction.parameters().size())
            Assert.assertTrue(generalFunction.parameters().contains("18"))
            assertEquals(3, trial.cohorts().size())
            val cohortA = findCohort(trial.cohorts(), "A")
            Assert.assertTrue(cohortA.metadata().evaluable())
            Assert.assertTrue(cohortA.metadata().open())
            Assert.assertTrue(cohortA.metadata().slotsAvailable())
            assertEquals("Cohort A", cohortA.metadata().description())
            assertEquals(1, cohortA.eligibility().size())
            val functionA = findBaseFunction(cohortA.eligibility(), EligibilityRule.NOT)
            assertEquals(1, functionA.parameters().size())
            val subFunctionA = findSubFunction(functionA.parameters(), EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES)
            Assert.assertTrue(subFunctionA.parameters().isEmpty())
            val cohortB = findCohort(trial.cohorts(), "B")
            Assert.assertTrue(cohortB.metadata().evaluable())
            Assert.assertTrue(cohortB.metadata().open())
            Assert.assertTrue(cohortB.metadata().slotsAvailable())
            assertEquals("Cohort B", cohortB.metadata().description())
            Assert.assertTrue(cohortB.eligibility().isEmpty())
            val cohortC = findCohort(trial.cohorts(), "C")
            Assert.assertFalse(cohortC.metadata().evaluable())
            Assert.assertFalse(cohortC.metadata().open())
            Assert.assertFalse(cohortC.metadata().slotsAvailable())
            assertEquals("Cohort C", cohortC.metadata().description())
            assertEquals(3, cohortC.eligibility().size())
            val functionC1 = findBaseFunction(cohortC.eligibility(), EligibilityRule.HAS_BIOPSY_AMENABLE_LESION)
            Assert.assertTrue(functionC1.parameters().isEmpty())
            val functionC2 = findBaseFunction(cohortC.eligibility(), EligibilityRule.OR)
            assertEquals(2, functionC2.parameters().size())
            val subFunction1 = findSubFunction(functionC2.parameters(), EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X)
            assertEquals(1, subFunction1.parameters().size())
            Assert.assertTrue(subFunction1.parameters().contains("cancer term"))
            val subFunction2 = findSubFunction(functionC2.parameters(), EligibilityRule.IS_PREGNANT)
            Assert.assertTrue(subFunction2.parameters().isEmpty())
        }

        private fun findCohort(cohorts: List<Cohort>, cohortId: String): Cohort {
            for (cohort in cohorts) {
                if (cohort.metadata().cohortId().equals(cohortId)) {
                    return cohort
                }
            }
            throw IllegalStateException("Could not find cohort with id: $cohortId")
        }

        private fun findBaseFunction(eligibility: List<Eligibility>, rule: EligibilityRule): EligibilityFunction {
            for (entry in eligibility) {
                if (entry.function().rule() === rule) {
                    return entry.function()
                }
            }
            throw IllegalStateException("Could not find base eligibility function with rule: $rule")
        }

        private fun <X> findSubFunction(functions: List<X>, rule: EligibilityRule): EligibilityFunction {
            for (function in functions) {
                val func = function as EligibilityFunction
                if (func.rule() === rule) {
                    return func
                }
            }
            throw IllegalStateException("Could not find sub function with rule: $rule")
        }
    }
}