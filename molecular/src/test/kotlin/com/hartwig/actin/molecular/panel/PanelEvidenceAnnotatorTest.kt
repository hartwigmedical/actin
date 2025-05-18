package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestGeneAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.driverlikelihood.TEST_ONCO_DNDS_TSV
import com.hartwig.actin.molecular.driverlikelihood.TEST_TSG_DNDS_TSV
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.datamodel.molecular.driver.GeneRole as actinGeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect as actinProteinEffect
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as serveProteinEffect

private val ACTIONABILITY_MATCH_FOR_VARIANT = mockk<ClinicalEvidence>()

private val ACTIONABILITY_MATCH_FOR_FUSION = mockk<ClinicalEvidence>()
private val KNOWN_FUSION = mockk<KnownFusion> {
    every { proteinEffect() } returns serveProteinEffect.GAIN_OF_FUNCTION
    every { associatedWithDrugResistance() } returns true
}

private val ACTIONABILITY_MATCH_FOR_COPY_NUMBER = mockk<ClinicalEvidence>()

private val AMPLIFICATION = TestGeneAlterationFactory.createGeneAlteration("gene 1", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true)

private const val ALT = "T"
private const val REF = "G"
private const val CHROMOSOME = "1"
private const val POSITION = 1

private val VARIANT = TestMolecularFactory.createMinimalVariant().copy(
    chromosome = CHROMOSOME,
    position = POSITION,
    ref = REF,
    alt = ALT,
    type = VariantType.SNV
)

private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()

private val HOTSPOT = TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true, true)
private val NON_HOTSPOT = TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)

class PanelEvidenceAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase>()
    private val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV))
    private val panelEvidenceAnnotator = PanelEvidenceAnnotator(evidenceDatabase, geneDriverLikelihoodModel)

    @Test
    fun `Should annotate variant that is hotspot`() {
        every { evidenceDatabase.evidenceForVariant(any()) } returns ACTIONABILITY_MATCH_FOR_VARIANT
        every { evidenceDatabase.alterationForVariant(any()) } returns HOTSPOT

        val panelRecord = panelRecordWith(VARIANT)
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.isHotspot).isTrue
        assertThat(annotatedVariant.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_VARIANT)
        assertThat(annotatedVariant.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedVariant.isAssociatedWithDrugResistance).isTrue
        assertThat(annotatedVariant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate variant that is not a hotspot`() {
        every { evidenceDatabase.evidenceForVariant(any()) } returns ACTIONABILITY_MATCH_FOR_VARIANT
        every { evidenceDatabase.alterationForVariant(any()) } returns NON_HOTSPOT

        val panelRecord = panelRecordWith(VARIANT.copy(isHotspot = false))
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.isHotspot).isFalse
        assertThat(annotatedVariant.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_VARIANT)
        assertThat(annotatedVariant.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(actinProteinEffect.NO_EFFECT)
        assertThat(annotatedVariant.isAssociatedWithDrugResistance).isFalse
        assertThat(annotatedVariant.driverLikelihood).isNull()
    }

    @Test
    fun `Should not annotate with evidence when no matches found`() {
        every { evidenceDatabase.evidenceForVariant(any()) } returns EMPTY_MATCH
        every { evidenceDatabase.alterationForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)

        val panelRecord = panelRecordWith(VARIANT.copy(gene = "other gene"))
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)
        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        assertThat(annotatedPanelRecord.drivers.variants.first().evidence).isEqualTo(TestClinicalEvidenceFactory.createEmpty())

    }

    @Test
    fun `Should annotate fusion`() {
        every { evidenceDatabase.evidenceForFusion(any()) } returns ACTIONABILITY_MATCH_FOR_FUSION
        every { evidenceDatabase.lookupKnownFusion(any()) } returns KNOWN_FUSION

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalFusion())
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.fusions).hasSize(1)
        val annotatedFusion = annotatedPanelRecord.drivers.fusions.first()

        assertThat(annotatedFusion.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_FUSION)
        assertThat(annotatedFusion.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedFusion.isAssociatedWithDrugResistance).isTrue
    }

    @Test
    fun `Should annotate copy number`() {
        every { evidenceDatabase.evidenceForCopyNumber(any()) } returns ACTIONABILITY_MATCH_FOR_COPY_NUMBER
        every { evidenceDatabase.alterationForCopyNumber(any()) } returns AMPLIFICATION

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalCopyNumber())
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.copyNumbers).hasSize(1)
        val annotatedCopyNumber = annotatedPanelRecord.drivers.copyNumbers.first()

        assertThat(annotatedCopyNumber.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_COPY_NUMBER)
        assertThat(annotatedCopyNumber.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedCopyNumber.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedCopyNumber.isAssociatedWithDrugResistance).isTrue
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