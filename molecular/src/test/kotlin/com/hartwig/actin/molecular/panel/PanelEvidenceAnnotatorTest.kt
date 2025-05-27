package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val ACTIONABILITY_MATCH_FOR_VARIANT = mockk<ClinicalEvidence>()
private val ACTIONABILITY_MATCH_FOR_FUSION = mockk<ClinicalEvidence>()
private val ACTIONABILITY_MATCH_FOR_COPY_NUMBER = mockk<ClinicalEvidence>()

class PanelEvidenceAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase>()
    private val panelEvidenceAnnotator = PanelEvidenceAnnotator(evidenceDatabase)

    @Test
    fun `Should annotate variant with evidence`() {
        every { evidenceDatabase.evidenceForVariant(any()) } returns ACTIONABILITY_MATCH_FOR_VARIANT

        val panelRecord = panelRecordWith(VARIANT)
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_VARIANT)
    }

    @Test
    fun `Should annotate fusion with evidence`() {
        every { evidenceDatabase.evidenceForFusion(any()) } returns ACTIONABILITY_MATCH_FOR_FUSION

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalFusion())
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.fusions).hasSize(1)
        val annotatedFusion = annotatedPanelRecord.drivers.fusions.first()
        assertThat(annotatedFusion.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_FUSION)
    }

    @Test
    fun `Should annotate copy number with evidence`() {
        every { evidenceDatabase.evidenceForCopyNumber(any()) } returns ACTIONABILITY_MATCH_FOR_COPY_NUMBER

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalCopyNumber())
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.copyNumbers).hasSize(1)
        val annotatedCopyNumber = annotatedPanelRecord.drivers.copyNumbers.first()

        assertThat(annotatedCopyNumber.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_COPY_NUMBER)
    }

    @Test
    fun `Should annotate microsatellite status with evidence`() {
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForMicrosatelliteStatus(false) } returns EMPTY_MATCH

        val panelWithMSI = panelEvidenceAnnotator.annotate(panelRecordWithMsi(isUnstable = true))
        assertThat(panelWithMSI.characteristics.microsatelliteStability!!.evidence).isEqualTo(ON_LABEL_MATCH)

        val panelWithMSS = panelEvidenceAnnotator.annotate(panelRecordWithMsi(isUnstable = false))
        assertThat(panelWithMSS.characteristics.microsatelliteStability!!.evidence).isEqualTo(EMPTY_MATCH)

        val panelWithoutMicrosatelliteStatus = panelEvidenceAnnotator.annotate(panelRecordWithMsi(isUnstable = null))
        assertThat(panelWithoutMicrosatelliteStatus.characteristics.microsatelliteStability).isNull()
    }

    @Test
    fun `Should annotate HR status with evidence`() {
        every { evidenceDatabase.evidenceForHomologousRecombinationStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForHomologousRecombinationStatus(false) } returns EMPTY_MATCH

        val panelWithHRD = panelEvidenceAnnotator.annotate(panelRecordWithHrd(isDeficient = true))
        assertThat(panelWithHRD.characteristics.homologousRecombination!!.evidence).isEqualTo(ON_LABEL_MATCH)

        val panelWithHRP = panelEvidenceAnnotator.annotate(panelRecordWithHrd(isDeficient = false))
        assertThat(panelWithHRP.characteristics.homologousRecombination!!.evidence).isEqualTo(EMPTY_MATCH)

        val panelWithoutHRStatus = panelEvidenceAnnotator.annotate(panelRecordWithHrd(isDeficient = null))
        assertThat(panelWithoutHRStatus.characteristics.homologousRecombination).isNull()
    }

    @Test
    fun `Should annotate tumor mutational burden with evidence`() {
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(true) } returns ON_LABEL_MATCH
        every { evidenceDatabase.evidenceForTumorMutationalBurdenStatus(false) } returns EMPTY_MATCH

        val panelWithHighTmb = panelEvidenceAnnotator.annotate(panelRecordWithTmb(isHigh = true))
        assertThat(panelWithHighTmb.characteristics.tumorMutationalBurden!!.evidence).isEqualTo(ON_LABEL_MATCH)

        val panelWithLowTmb = panelEvidenceAnnotator.annotate(panelRecordWithTmb(isHigh = false))
        assertThat(panelWithLowTmb.characteristics.tumorMutationalBurden!!.evidence).isEqualTo(EMPTY_MATCH)

        val panelWithoutTmb = panelEvidenceAnnotator.annotate(panelRecordWithTmb(isHigh = null))
        assertThat(panelWithoutTmb.characteristics.tumorMutationalBurden).isNull()
    }

    private fun panelRecordWithMsi(isUnstable: Boolean?): PanelRecord {
        val characteristics = TestMolecularFactory.createMinimalTestCharacteristics()

        val microsatelliteStability = isUnstable?.let {
            MicrosatelliteStability(
                microsatelliteIndelsPerMb = if (isUnstable) 100.0 else 0.0,
                isUnstable = isUnstable,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }

        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = characteristics.copy(
                microsatelliteStability = microsatelliteStability
            )
        )
    }

    private fun panelRecordWithHrd(isDeficient: Boolean?): PanelRecord {
        val characteristics = TestMolecularFactory.createMinimalTestCharacteristics()

        val homologousRecombination = isDeficient?.let {
            HomologousRecombination(
                score = null,
                isDeficient = isDeficient,
                type = null,
                brca1Value = null,
                brca2Value = null,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }

        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = characteristics.copy(
                homologousRecombination = homologousRecombination
            )
        )
    }

    private fun panelRecordWithTmb(isHigh: Boolean?): PanelRecord {
        val characteristics = TestMolecularFactory.createMinimalTestCharacteristics()

        val tumorMutationalBurden = isHigh?.let {
            TumorMutationalBurden(
                score = if (isHigh) 100.0 else 0.0,
                isHigh = isHigh,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        }

        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            characteristics = characteristics.copy(
                tumorMutationalBurden = tumorMutationalBurden
            )
        )
    }

    private fun panelRecordWith(variant: Variant): PanelRecord {
        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                variants = listOf(variant)
            )
        )
    }

    private fun panelRecordWith(fusion: Fusion): PanelRecord {
        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                fusions = listOf(fusion)
            )
        )
    }

    private fun panelRecordWith(copyNumber: CopyNumber): PanelRecord {
        return TestMolecularFactory.createMinimalTestPanelRecord().copy(
            drivers = TestMolecularFactory.createMinimalTestDrivers().copy(
                copyNumbers = listOf(copyNumber)
            )
        )
    }
}