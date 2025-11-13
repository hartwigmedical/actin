package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.trial.TrialPhase
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohortTestFactory
import com.hartwig.actin.report.trial.ExternalTrials
import com.hartwig.actin.report.trial.MolecularFilteredExternalTrials
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EligibleTrialGeneratorTest {

    val externalTrials = ExternalTrials(
        MolecularFilteredExternalTrials(emptySet(), emptySet()),
        MolecularFilteredExternalTrials(emptySet(), emptySet())
    )
    val requestingSource = TrialSource.EXAMPLE
    val countryOfReference = Country.NETHERLANDS

    @Test
    fun `Should filter early phase open and eligible national cohorts correctly`() {
        val cohortToRemain1 = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "1",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = false,
            phase = TrialPhase.PHASE_1
        )
        val cohortToRemain2 = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "2",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = false,
            phase = null
        )
        val cohortToFilter1 = cohortToRemain1.copy(isPotentiallyEligible = false)
        val cohortToFilter2 = cohortToRemain1.copy(isOpen = false)
        val cohortToFilter3 = cohortToRemain1.copy(isMissingMolecularResultForEvaluation = true)
        val cohortToFilter4 = cohortToRemain1.copy(phase = TrialPhase.PHASE_3)

        val cohorts =
            listOf(cohortToRemain1, cohortToRemain2, cohortToFilter1, cohortToFilter2, cohortToFilter3, cohortToFilter4)
        val trialType = TrialType.LOCAL_EARLY_PHASE
        val result = EligibleTrialGenerator.nationalOpenCohorts(cohorts, externalTrials, requestingSource, countryOfReference, trialType)

        assertThat(result.cohortSize()).isEqualTo(2)
        assertThat(result.title()).isEqualTo("Phase 1 (or unknown phase) trials in NL that are open and potentially eligible (2 cohorts from 2 trials)")
    }

    @Test
    fun `Should filter late phase open and eligible national cohorts correctly`() {
        val cohortToRemain = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "1",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = false,
            phase = TrialPhase.PHASE_4
        )
        val cohortToFilter1 = cohortToRemain.copy(isPotentiallyEligible = false)
        val cohortToFilter2 = cohortToRemain.copy(isOpen = false)
        val cohortToFilter3 = cohortToRemain.copy(isMissingMolecularResultForEvaluation = true)
        val cohortToFilter4 = cohortToRemain.copy(phase = TrialPhase.PHASE_1_2)

        val cohorts = listOf(cohortToRemain, cohortToFilter1, cohortToFilter2, cohortToFilter3, cohortToFilter4)
        val trialType = TrialType.LOCAL_LATE_PHASE
        val result = EligibleTrialGenerator.nationalOpenCohorts(cohorts, externalTrials, requestingSource, countryOfReference, trialType)

        assertThat(result.cohortSize()).isEqualTo(1)
        assertThat(result.title()).isEqualTo("Phase 2/3+ trials in NL that are open and potentially eligible (1 cohort from 1 trial)")
    }

    @Test
    fun `Should filter cohorts with missing molecular test correctly`() {
        val cohortToRemain1 = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "1",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = true,
            phase = TrialPhase.PHASE_1
        )
        val cohortToRemain2 = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "2",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = true,
            phase = TrialPhase.PHASE_4
        )
        val cohortToRemain3 = InterpretedCohortTestFactory.interpretedCohort(
            trialId = "3",
            isPotentiallyEligible = true,
            isOpen = true,
            isMissingMolecularResultForEvaluation = true,
            phase = null
        )
        val cohortToFilter1 = cohortToRemain1.copy(isPotentiallyEligible = false)
        val cohortToFilter2 = cohortToRemain1.copy(isOpen = false)
        val cohortToFilter3 = cohortToRemain1.copy(isMissingMolecularResultForEvaluation = false)
        val cohortToFilter4 = cohortToRemain2.copy(isMissingMolecularResultForEvaluation = false)
        val cohortToFilter5 = cohortToRemain3.copy(isMissingMolecularResultForEvaluation = false)

        val cohorts = listOf(
            cohortToRemain1,
            cohortToRemain2,
            cohortToRemain3,
            cohortToFilter1,
            cohortToFilter2,
            cohortToFilter3,
            cohortToFilter4,
            cohortToFilter5
        )
        val result = EligibleTrialGenerator.openCohortsWithMissingMolecularResultsForEvaluation(cohorts, requestingSource)

        assertThat(result?.cohortSize()).isEqualTo(3)
        assertThat(result?.title()).isEqualTo("Trials in NL that are open but additional molecular tests needed to evaluate eligibility (3 cohorts from 3 trials)")
    }
}