package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.VirusType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.serve.datamodel.molecular.characteristic.TumorCharacteristicType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val HPV_EVIDENCE = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HPV_POSITIVE)
private val HPV_TRIAL = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HPV_POSITIVE)

private val EBV_EVIDENCE = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.EBV_POSITIVE)
private val EBV_TRIAL = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.EBV_POSITIVE)

private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForCharacteristic(TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN)

class VirusEvidenceTest {

    private val virusEvidence = VirusEvidence.create(
        evidences = listOf(HPV_EVIDENCE, EBV_EVIDENCE, OTHER_EVIDENCE),
        trials = listOf(HPV_TRIAL, EBV_TRIAL, OTHER_TRIAL)
    )

    @Test
    fun `Should determine evidence and trials for HPV`() {
        val virusMatch = TestMolecularFactory.minimalVirus().copy(type = VirusType.HUMAN_PAPILLOMA_VIRUS, isReportable = true)

        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidenceMatches).containsExactly(HPV_EVIDENCE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(HPV_TRIAL to HPV_TRIAL.anyMolecularCriteria()))
    }

    @Test
    fun `Should determine evidence and trials for EBV`() {
        val virusMatch = TestMolecularFactory.minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = true)

        val matches = virusEvidence.findMatches(virusMatch)
        assertThat(matches.evidenceMatches).containsExactly(EBV_EVIDENCE)
        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(mapOf(EBV_TRIAL to EBV_TRIAL.anyMolecularCriteria()))
    }

    @Test
    fun `Should find no evidence or trials on other reportable virus type`() {
        val otherVirus = TestMolecularFactory.minimalVirus().copy(type = VirusType.MERKEL_CELL_VIRUS, isReportable = true)

        val matches = virusEvidence.findMatches(otherVirus)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should find no evidence or trials for unreported EBV`() {
        val notReported = TestMolecularFactory.minimalVirus().copy(type = VirusType.EPSTEIN_BARR_VIRUS, isReportable = false)

        val matches = virusEvidence.findMatches(notReported)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }
}