package com.hartwig.actin.algo

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.CurrentDateProvider
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceTestFactory
import com.hartwig.actin.algo.datamodel.EvaluatedTreatment
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentCandidate
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.interpretation.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.RecommendationEngine
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TestTrialFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class TreatmentMatcherTest {
    private val patient = TestDataFactory.createMinimalTestPatientRecord()
    private val trials = listOf(TestTrialFactory.createMinimalTestTrial())
    private val trialMatches = TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches
    private val trialMatcher = mockk<TrialMatcher> {
        every { determineEligibility(patient, trials) } returns trialMatches
    }
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val evidenceEntries =
        EfficacyEntryFactory(treatmentDatabase).convertCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase())
    private val recommendationEngine = mockk<RecommendationEngine>()
    private val treatmentMatcher = TreatmentMatcher(
        trialMatcher,
        recommendationEngine,
        trials,
        CurrentDateProvider(),
        EvaluatedTreatmentAnnotator.create(evidenceEntries),
        EMC_TRIAL_SOURCE
    )
    private val expectedTreatmentMatch = TreatmentMatch(
        patientId = patient.patientId,
        sampleId = patient.molecular?.sampleId ?: "N/A",
        referenceDate = LocalDate.now(),
        referenceDateIsLive = true,
        trialMatches = trialMatches,
        standardOfCareMatches = null,
        trialSource = EMC_TRIAL_SOURCE
    )

    @Test
    fun `Should produce match for patient when SOC evaluation unavailable and annotate with efficacy evidence`() {
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(patient) } returns false
        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patient)).isEqualTo(expectedTreatmentMatch)
    }

    @Test
    fun `Should include SOC evaluations for patient when SOC evaluation is available`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("test", TreatmentCategory.CHEMOTHERAPY), false, setOf(eligibilityFunction)
        )
        val expectedSocTreatments = listOf(EvaluatedTreatment(treatmentCandidate, listOf(EvaluationFactory.pass("Has MSI"))))

        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(patient) } returns true
        every { recommendationEngine.standardOfCareEvaluatedTreatments(patient) } returns expectedSocTreatments

        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patient))
            .isEqualTo(
                expectedTreatmentMatch.copy(
                    standardOfCareMatches = EvaluatedTreatmentAnnotator.create(evidenceEntries).annotate(
                        expectedSocTreatments
                    )
                )
            )
    }

    @Test
    fun `Should match without molecular input`() {
        val patientWithoutMolecular = patient.copy(molecular = null)
        val trialMatcher = mockk<TrialMatcher> {
            every { determineEligibility(patientWithoutMolecular, trials) } returns trialMatches
        }
        val treatmentMatcher = TreatmentMatcher(trialMatcher, recommendationEngine, trials, CurrentDateProvider(),
            EvaluatedTreatmentAnnotator.create(evidenceEntries), EMC_TRIAL_SOURCE)
        every { recommendationEngine.standardOfCareCanBeEvaluatedForPatient(patientWithoutMolecular) } returns false
        val expectedTreatmentMatchWithoutMolecular = expectedTreatmentMatch.copy(sampleId = "N/A")

        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patientWithoutMolecular)).isEqualTo(expectedTreatmentMatchWithoutMolecular)
    }
}