package com.hartwig.actin.algo

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.CurrentDateProvider
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceTestFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evidence.TreatmentRanker
import com.hartwig.actin.algo.soc.EvaluatedTreatmentAnnotator
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.soc.StandardOfCareEvaluation
import com.hartwig.actin.algo.soc.StandardOfCareEvaluator
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.algo.TreatmentCandidate
import com.hartwig.actin.datamodel.algo.TreatmentEvidenceRanking
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import com.hartwig.actin.datamodel.trial.TestTrialFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val MAX_AGE = LocalDate.of(2023, 12, 10)

class TreatmentMatcherTest {
    private val patient = TestPatientFactory.createMinimalTestWGSPatientRecord()
    private val trials = listOf(TestTrialFactory.createMinimalTestTrial())
    private val trialMatches = TestTreatmentMatchFactory.createProperTreatmentMatch().trialMatches
    private val trialMatcher = mockk<TrialMatcher> {
        every { determineEligibility(patient, trials) } returns trialMatches
    }
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val evidenceEntries =
        EfficacyEntryFactory(treatmentDatabase).convertCkbExtendedEvidence(CkbExtendedEvidenceTestFactory.createProperTestExtendedEvidenceDatabase())
    private val evidences: List<EfficacyEvidence> = emptyList()
    private val standardOfCareEvaluator = mockk<StandardOfCareEvaluator>()
    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val resistanceEvidenceMatcher = ResistanceEvidenceMatcher.create(
        doidModel,
        emptySet(),
        evidences,
        treatmentDatabase,
        TestMolecularFactory.createMinimalTestMolecularHistory()
    )
    private val treatmentMatcher = TreatmentMatcher(
        trialMatcher,
        standardOfCareEvaluator,
        trials,
        CurrentDateProvider(),
        EvaluatedTreatmentAnnotator.create(evidenceEntries, resistanceEvidenceMatcher),
        null,
        MAX_AGE,
        TreatmentRanker()
    )
    private val expectedTreatmentMatch = TreatmentMatch(
        patientId = patient.patientId,
        sampleId = patient.molecularHistory.latestOrangeMolecularRecord()?.sampleId ?: "N/A",
        referenceDate = LocalDate.now(),
        referenceDateIsLive = true,
        trialMatches = trialMatches,
        standardOfCareMatches = null,
        maxMolecularTestAge = MAX_AGE,
        treatmentEvidenceRanking = TreatmentEvidenceRanking(emptyList())
    )

    @Test
    fun `Should produce match for patient when SOC evaluation unavailable and annotate with efficacy evidence`() {
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patient) } returns false
        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patient)).isEqualTo(expectedTreatmentMatch)
    }

    @Test
    fun `Should include SOC evaluations for patient when SOC evaluation is available`() {
        val eligibilityFunction = EligibilityFunction(EligibilityRule.MSI_SIGNATURE, emptyList())
        val treatmentCandidate = TreatmentCandidate(
            TreatmentTestFactory.drugTreatment("test", TreatmentCategory.CHEMOTHERAPY), false, setOf(eligibilityFunction)
        )
        val expectedSocTreatments = listOf(EvaluatedTreatment(treatmentCandidate, listOf(EvaluationFactory.pass("Has MSI"))))

        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patient) } returns true
        every { standardOfCareEvaluator.standardOfCareEvaluatedTreatments(patient) } returns StandardOfCareEvaluation(expectedSocTreatments)

        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patient))
            .isEqualTo(
                expectedTreatmentMatch.copy(
                    standardOfCareMatches = EvaluatedTreatmentAnnotator.create(evidenceEntries, resistanceEvidenceMatcher)
                        .annotate(expectedSocTreatments)
                )
            )
    }

    @Test
    fun `Should match without molecular input`() {
        val patientWithoutMolecular = patient.copy(molecularHistory = MolecularHistory.empty())
        val trialMatcher = mockk<TrialMatcher> {
            every { determineEligibility(patientWithoutMolecular, trials) } returns trialMatches
        }
        val treatmentMatcher = TreatmentMatcher(
            trialMatcher,
            standardOfCareEvaluator,
            trials,
            CurrentDateProvider(),
            EvaluatedTreatmentAnnotator.create(evidenceEntries, resistanceEvidenceMatcher),
            null,
            MAX_AGE,
            TreatmentRanker()
        )
        every { standardOfCareEvaluator.standardOfCareCanBeEvaluatedForPatient(patientWithoutMolecular) } returns false
        val expectedTreatmentMatchWithoutMolecular = expectedTreatmentMatch.copy(sampleId = "N/A")

        assertThat(treatmentMatcher.evaluateAndAnnotateMatchesForPatient(patientWithoutMolecular)).isEqualTo(
            expectedTreatmentMatchWithoutMolecular
        )
    }
}