package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EVIDENCE_FOR_MSI = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
private val EVIDENCE_FOR_HRD =
    TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
private val EVIDENCE_FOR_HIGH_TMB =
    TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
private val EVIDENCE_FOR_HIGH_TML =
    TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE)

private val TRIAL_FOR_MSI = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.MICROSATELLITE_UNSTABLE)
private val TRIAL_FOR_HRD = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT)
private val TRIAL_FOR_HIGH_TMB = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
private val TRIAL_FOR_HIGH_TML = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD)
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HPV_POSITIVE)

class SignatureEvidenceTest {

    private val signatureEvidence = SignatureEvidence.create(
        evidences = listOf(EVIDENCE_FOR_MSI, EVIDENCE_FOR_HRD, EVIDENCE_FOR_HIGH_TMB, EVIDENCE_FOR_HIGH_TML, OTHER_EVIDENCE),
        trials = listOf(TRIAL_FOR_MSI, TRIAL_FOR_HRD, TRIAL_FOR_HIGH_TMB, TRIAL_FOR_HIGH_TML, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for microsatellite instability`() {
        val msiMatches = signatureEvidence.findMicrosatelliteMatches(true)
        assertThat(msiMatches.evidenceMatches).containsExactly(EVIDENCE_FOR_MSI)
        assertThat(msiMatches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(TRIAL_FOR_MSI to TRIAL_FOR_MSI.anyMolecularCriteria()))

        val mssMatches = signatureEvidence.findMicrosatelliteMatches(false)
        assertThat(mssMatches.evidenceMatches).isEmpty()
        assertThat(mssMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should determine evidence and trials for homologous repair deficiency`() {
        val hrdMatches = signatureEvidence.findHomologousRepairMatches(true)
        assertThat(hrdMatches.evidenceMatches).containsExactly(EVIDENCE_FOR_HRD)
        assertThat(hrdMatches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(TRIAL_FOR_HRD to TRIAL_FOR_HRD.anyMolecularCriteria()))

        val hrpMatches = signatureEvidence.findHomologousRepairMatches(false)
        assertThat(hrpMatches.evidenceMatches).isEmpty()
        assertThat(hrpMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should determine evidence and trials for high tumor mutational burden`() {
        val highTmbMatches = signatureEvidence.findTumorBurdenMatches(true)
        assertThat(highTmbMatches.evidenceMatches).containsExactly(EVIDENCE_FOR_HIGH_TMB)
        assertThat(highTmbMatches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(TRIAL_FOR_HIGH_TMB to TRIAL_FOR_HIGH_TMB.anyMolecularCriteria())
        )

        val lowTmbMatches = signatureEvidence.findTumorBurdenMatches(false)
        assertThat(lowTmbMatches.evidenceMatches).isEmpty()
        assertThat(lowTmbMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should determine evidence and trials for high mutational load`() {
        val highTmlMatches = signatureEvidence.findTumorLoadMatches(true)
        assertThat(highTmlMatches.evidenceMatches).containsExactly(EVIDENCE_FOR_HIGH_TML)
        assertThat(highTmlMatches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(TRIAL_FOR_HIGH_TML to TRIAL_FOR_HIGH_TML.anyMolecularCriteria())
        )

        val lowTmlMatches = signatureEvidence.findTumorLoadMatches(false)
        assertThat(lowTmlMatches.evidenceMatches).isEmpty()
        assertThat(lowTmlMatches.matchingCriteriaPerTrialMatch).isEmpty()
    }
}