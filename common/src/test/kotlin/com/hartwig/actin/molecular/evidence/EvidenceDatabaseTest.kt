package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import com.hartwig.actin.molecular.evidence.matching.FusionMatchCriteria
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EvidenceDatabaseTest {

    private val database = TestEvidenceDatabaseFactory.createProperDatabase()

    @Test
    fun `Should match evidence for variants`() {
        val variant = VariantMatchCriteria(
            gene = "",
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            driverLikelihood = DriverLikelihood.HIGH,
            isReportable = true
        )
        assertThat(database.geneAlterationForVariant(variant)).isNotNull()
        assertEvidence(database.evidenceForVariant(variant), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
    }

    @Test
    fun `Should match evidence for gains and losses`() {
        val loss = createWithCopyNumberType(CopyNumberType.LOSS)
        assertThat(database.geneAlterationForCopyNumber(loss)).isNotNull()
        assertEvidence(database.evidenceForCopyNumber(loss), expectedTreatmentMatches = 1, expectedTrialMatches = 1)

        val gain = createWithCopyNumberType(CopyNumberType.FULL_GAIN)
        assertThat(database.geneAlterationForCopyNumber(gain)).isNotNull()
        assertEvidence(database.evidenceForCopyNumber(gain), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
    }

    @Test
    fun `Should match evidence for disruption`() {
        val disruption = TestMolecularFactory.minimalDisruption().copy(isReportable = true)
        assertThat(database.geneAlterationForDisruption(disruption)).isNotNull()
        assertEvidence(database.evidenceForDisruption(disruption), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
    }

    @Test
    fun `Should match evidence for homozygous disruption`() {
        val homozygousDisruption = TestMolecularFactory.minimalHomozygousDisruption()
        assertThat(database.geneAlterationForHomozygousDisruption(homozygousDisruption)).isNotNull()
        assertEvidence(
            database.evidenceForHomozygousDisruption(homozygousDisruption),
            expectedTreatmentMatches = 2,
            expectedTrialMatches = 2
        )
    }

    @Test
    fun `Should match evidence for fusion`() {
        val fusion = FusionMatchCriteria(
            isReportable = true,
            geneStart = "",
            fusedExonUp = 0,
            geneEnd = "",
            fusedExonDown = 0,
            driverType = FusionDriverType.NONE,
        )
        assertThat(database.lookupKnownFusion(fusion)).isNotNull()
        assertEvidence(database.evidenceForFusion(fusion), expectedTreatmentMatches = 2, expectedTrialMatches = 2)
    }

    @Test
    fun `Should match evidence for viruses`() {
        val hpv = TestMolecularFactory.minimalVirus().copy(isReportable = true, type = VirusType.HUMAN_PAPILLOMA_VIRUS)
        assertEvidence(database.evidenceForVirus(hpv), expectedTreatmentMatches = 1, expectedTrialMatches = 1)

        val ebv = TestMolecularFactory.minimalVirus().copy(isReportable = true, type = VirusType.EPSTEIN_BARR_VIRUS)
        assertEvidence(database.evidenceForVirus(ebv), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
    }

    @Test
    fun `Should match evidence for signatures`() {
        assertEvidence(database.evidenceForMicrosatelliteStatus(false), expectedTreatmentMatches = 0, expectedTrialMatches = 0)
        assertEvidence(database.evidenceForMicrosatelliteStatus(true), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
        assertEvidence(database.evidenceForHomologousRecombinationStatus(false), expectedTreatmentMatches = 0, expectedTrialMatches = 0)
        assertEvidence(database.evidenceForHomologousRecombinationStatus(true), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
        assertEvidence(database.evidenceForTumorMutationalBurdenStatus(false), expectedTreatmentMatches = 0, expectedTrialMatches = 0)
        assertEvidence(database.evidenceForTumorMutationalBurdenStatus(true), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
        assertEvidence(database.evidenceForTumorMutationalLoadStatus(false), expectedTreatmentMatches = 0, expectedTrialMatches = 0)
        assertEvidence(database.evidenceForTumorMutationalLoadStatus(true), expectedTreatmentMatches = 1, expectedTrialMatches = 1)
    }

    private fun assertEvidence(evidence: ClinicalEvidence, expectedTreatmentMatches: Int, expectedTrialMatches: Int = 0) {
        assertThat(evidence.treatmentEvidence.size).isEqualTo(expectedTreatmentMatches)
        assertThat(evidence.eligibleTrials.size).isEqualTo(expectedTrialMatches)
    }

    private fun createWithCopyNumberType(type: CopyNumberType): CopyNumber {
        return TestMolecularFactory.minimalCopyNumber().copy(
            isReportable = true,
            canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact(type)
        )
    }
}