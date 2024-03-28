package com.hartwig.actin.trial.interpretation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory
import com.hartwig.actin.testutil.resourceOnClasspath
import com.hartwig.actin.trial.config.TestTrialConfigDatabaseFactory
import com.hartwig.actin.trial.config.TrialConfigDatabaseValidator
import com.hartwig.actin.trial.config.TrialConfigModel
import com.hartwig.actin.trial.datamodel.Cohort
import com.hartwig.actin.trial.datamodel.Eligibility
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.Trial
import com.hartwig.actin.trial.status.TestTrialStatusConfigInterpreterFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialIngestionTest {
    private val eligibilityFactory = TestEligibilityFactoryFactory.createTestEligibilityFactory()

    @Test
    fun `Should not crash when creating from trial config directory`() {
        assertThat(
            TrialIngestion.create(
                TRIAL_CONFIG_DIRECTORY,
                TestTrialStatusConfigInterpreterFactory.createWithMinimalTestTrialStatusDatabase(),
                TestDoidModelFactory.createMinimalTestDoidModel(),
                TestGeneFilterFactory.createNeverValid(),
                TreatmentDatabase(emptyMap(), emptyMap()),
                MedicationCategories(emptyMap(), AtcTree(emptyMap()))
            )
        ).isNotNull
    }

    @Test
    fun `Should create expected trials from proper test model`() {
        val ingestion = TrialIngestion(
            TrialConfigModel.createFromDatabase(
                TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase(),
                TrialConfigDatabaseValidator(eligibilityFactory)
            ),
            TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase(),
            eligibilityFactory
        )
        val ingestionResult = ingestion.ingestTrials()
        assertThat(ingestionResult.trials).hasSize(2)

        val trial = findTrial(ingestionResult.trials, "TEST-1")
        assertThat(trial.identification.open).isTrue
        assertThat(trial.identification.acronym).isEqualTo("Acronym-TEST-1")
        assertThat(trial.identification.title).isEqualTo("Title for TEST-1")
        assertThat(trial.generalEligibility).hasSize(1)

        val generalFunction = findFunction(trial.generalEligibility, EligibilityRule.IS_AT_LEAST_X_YEARS_OLD)
        assertThat(generalFunction.parameters).hasSize(1)
        assertThat(trial.cohorts).hasSize(3)

        val cohortA = findCohort(trial.cohorts, "A")
        assertThat(cohortA.metadata.description).isEqualTo("Cohort A")
        assertThat(cohortA.eligibility).hasSize(2)

        val cohortFunction1 = findFunction(cohortA.eligibility, EligibilityRule.HAS_INR_ULN_OF_AT_MOST_X)
        assertThat(cohortFunction1.parameters).containsExactly("1")

        val cohortFunction2 = findFunction(cohortA.eligibility, EligibilityRule.NOT)
        assertThat(cohortFunction2.parameters).hasSize(1)

        val subFunction = cohortFunction2.parameters[0] as EligibilityFunction
        assertThat(subFunction.rule).isEqualTo(EligibilityRule.OR)
        assertThat(subFunction.parameters).hasSize(2)

        val cohortB = findCohort(trial.cohorts, "B")
        assertThat(cohortB.metadata.description).isEqualTo("Cohort B")
        assertThat(cohortB.eligibility).isEmpty()

        val cohortC = findCohort(trial.cohorts, "C")
        assertThat(cohortC.metadata.description).isEqualTo("Cohort C")
        assertThat(cohortC.eligibility).isEmpty()
    }

    @Test(expected = IllegalStateException::class)
    fun `Should crash in case trial status cannot be resolved`() {
        val trialConfigDatabase = TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()
        val ingestion = TrialIngestion(
            TrialConfigModel.createFromDatabase(
                trialConfigDatabase.copy(
                    trialDefinitionConfigs = trialConfigDatabase.trialDefinitionConfigs.map { it.copy(open = null) }
                ),
                TrialConfigDatabaseValidator(eligibilityFactory)
            ),
            TestTrialStatusConfigInterpreterFactory.createWithMinimalTestTrialStatusDatabase(),
            eligibilityFactory
        )

        ingestion.ingestTrials()
    }

    @Test
    fun `Should not create trials when there are invalid inclusion criteria`() {
        val baseConfigDatabase = TestTrialConfigDatabaseFactory.createProperTestTrialConfigDatabase()
        val trialConfigDatabase = baseConfigDatabase.copy(
            inclusionCriteriaConfigs = baseConfigDatabase.inclusionCriteriaConfigs.map { it.copy(inclusionRule = "INVALID") }
        )
        val ingestion = TrialIngestion(
            TrialConfigModel.createFromDatabase(trialConfigDatabase, TrialConfigDatabaseValidator(eligibilityFactory)),
            TestTrialStatusConfigInterpreterFactory.createWithProperTestTrialStatusDatabase(),
            eligibilityFactory
        )
        assertThat(ingestion.ingestTrials().trials).isEmpty()
    }

    companion object {
        private val TRIAL_CONFIG_DIRECTORY = resourceOnClasspath("trial_config")

        private fun findTrial(trials: List<Trial>, trialId: String): Trial {
            return trials.firstOrNull { it.identification.trialId == trialId }
                ?: throw IllegalStateException("Could not find trial with ID: $trialId")
        }

        private fun findCohort(cohorts: List<Cohort>, cohortId: String): Cohort {
            return cohorts.firstOrNull { it.metadata.cohortId == cohortId }
                ?: throw IllegalStateException("Could not find cohort with ID: $cohortId")
        }

        private fun findFunction(eligibility: List<Eligibility>, rule: EligibilityRule): EligibilityFunction {
            return eligibility.firstOrNull { it.function.rule == rule }?.function
                ?: throw IllegalStateException("Could not find eligibility function with rule: $rule")
        }
    }
}