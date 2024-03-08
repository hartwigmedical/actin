package com.hartwig.actin.trial.serialization

import com.google.common.io.Resources
import com.hartwig.actin.trial.datamodel.Cohort
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TestTrialFactory
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.serialization.TrialJson.fromJson
import com.hartwig.actin.trial.serialization.TrialJson.readFromDir
import com.hartwig.actin.trial.serialization.TrialJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialJsonTest {

    private val trialDirectory = Resources.getResource("treatment").path

    @Test
    fun `Should convert to and from JSON`() {
        val minimal = TestTrialFactory.createMinimalTestTrial()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)
        val proper = TestTrialFactory.createProperTestTrial()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
    }

    @Test
    fun `Should read JSON records from directory`() {
        val trials = readFromDir(trialDirectory)
        assertThat(trials).hasSize(1)
        assertTrial(trials[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `Should throw exception when trying to read from regular file`() {
        readFromDir("$trialDirectory/file.json")
    }

    private fun assertTrial(trial: Trial) {
        assertThat(trial.identification.trialId).isEqualTo("test trial")
        assertThat(trial.identification.acronym).isEqualTo("TEST-TRIAL")
        assertThat(trial.identification.title).isEqualTo("This is a trial to test ACTIN")
        assertThat(trial.generalEligibility).hasSize(1)

        val generalFunction = findBaseFunction(trial.generalEligibility, EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
        assertThat(generalFunction.parameters).hasSize(1)
        assertThat(generalFunction.parameters).contains("18")

        assertThat(trial.cohorts).hasSize(3)
        val cohortA = findCohort(trial.cohorts, "A")
        assertThat(cohortA.metadata.evaluable).isTrue
        assertThat(cohortA.metadata.open).isTrue
        assertThat(cohortA.metadata.slotsAvailable).isTrue
        assertThat(cohortA.metadata.description).isEqualTo("Cohort A")
        assertThat(cohortA.eligibility).hasSize(1)

        val functionA = findBaseFunction(cohortA.eligibility, EligibilityRule.NOT)
        assertThat(functionA.parameters).hasSize(1)

        val subFunctionA = findSubFunction(functionA.parameters, EligibilityRule.HAS_KNOWN_ACTIVE_CNS_METASTASES)
        assertThat(subFunctionA.parameters).isEmpty()

        val cohortB = findCohort(trial.cohorts, "B")
        assertThat(cohortB.metadata.evaluable).isTrue
        assertThat(cohortB.metadata.open).isTrue
        assertThat(cohortB.metadata.slotsAvailable).isTrue
        assertThat(cohortB.metadata.description).isEqualTo("Cohort B")
        assertThat(cohortB.eligibility).isEmpty()

        val cohortC = findCohort(trial.cohorts, "C")
        assertThat(cohortC.metadata.evaluable).isFalse
        assertThat(cohortC.metadata.open).isFalse
        assertThat(cohortC.metadata.slotsAvailable).isFalse
        assertThat(cohortC.metadata.description).isEqualTo("Cohort C")
        assertThat(cohortC.eligibility).hasSize(3)

        val functionC1 = findBaseFunction(cohortC.eligibility, EligibilityRule.HAS_BIOPSY_AMENABLE_LESION)
        assertThat(functionC1.parameters).isEmpty()

        val functionC2 = findBaseFunction(cohortC.eligibility, EligibilityRule.OR)
        assertThat(functionC2.parameters).hasSize(2)

        val subFunction1 = findSubFunction(functionC2.parameters, EligibilityRule.HAS_PRIMARY_TUMOR_LOCATION_BELONGING_TO_DOID_TERM_X)
        assertThat(subFunction1.parameters).hasSize(1)
        assertThat(subFunction1.parameters).contains("cancer term")

        val subFunction2 = findSubFunction(functionC2.parameters, EligibilityRule.IS_PREGNANT)
        assertThat(subFunction2.parameters).isEmpty()
    }

    private fun findCohort(cohorts: List<Cohort>, cohortId: String): Cohort {
        return cohorts.find { cohort -> cohort.metadata.cohortId == cohortId }
            ?: throw IllegalStateException("Could not find cohort with id: $cohortId")
    }

    private fun findBaseFunction(eligibility: List<Eligibility>, rule: EligibilityRule): EligibilityFunction {
        return eligibility.find { entry -> entry.function.rule == rule }?.function
            ?: throw IllegalStateException("Could not find base eligibility function with rule: $rule")
    }

    private fun findSubFunction(functions: List<Any>, rule: EligibilityRule): EligibilityFunction {
        return functions.map { it as EligibilityFunction }.find { it.rule == rule }
            ?: throw IllegalStateException("Could not find sub function with rule: $rule")
    }
}