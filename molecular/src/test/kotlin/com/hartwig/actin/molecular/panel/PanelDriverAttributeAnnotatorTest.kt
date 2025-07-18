package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.panel.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestGeneAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.TEST_ONCO_DNDS_TSV
import com.hartwig.actin.molecular.driverlikelihood.TEST_TSG_DNDS_TSV
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.datamodel.molecular.driver.GeneRole as actinGeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect as actinProteinEffect
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as serveProteinEffect

private val KNOWN_FUSION = mockk<KnownFusion> {
    every { proteinEffect() } returns serveProteinEffect.GAIN_OF_FUNCTION
    every { associatedWithDrugResistance() } returns true
}

private val AMPLIFICATION = TestGeneAlterationFactory.createGeneAlteration("gene 1", GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true)

private val CANCER_ASSOCIATED_VARIANT =
    TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true, true)
private val NON_CANCER_ASSOCIATED_VARIANT =
    TestVariantAlterationFactory.createVariantAlteration(GENE, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)

class PanelDriverAttributeAnnotatorTest {

    private val knownEventResolver = mockk<KnownEventResolver>()
    private val panelDriverAttributeAnnotator = PanelDriverAttributeAnnotator(
        knownEventResolver,
        DndsDatabase.create(TEST_ONCO_DNDS_TSV, TEST_TSG_DNDS_TSV)
    )

    @Test
    fun `Should annotate variant that is a cancer-associated variant`() {
        every { knownEventResolver.resolveForVariant(any()) } returns CANCER_ASSOCIATED_VARIANT

        val panelRecord = panelRecordWith(VARIANT)
        val annotatedPanelRecord = panelDriverAttributeAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.isCancerAssociatedVariant).isTrue
        assertThat(annotatedVariant.evidence).isEqualTo(EMPTY_MATCH)
        assertThat(annotatedVariant.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedVariant.isAssociatedWithDrugResistance).isTrue
        assertThat(annotatedVariant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate variant that is not a cancer-associated variant`() {
        every { knownEventResolver.resolveForVariant(any()) } returns NON_CANCER_ASSOCIATED_VARIANT

        val panelRecord = panelRecordWith(VARIANT.copy(isCancerAssociatedVariant = false))
        val annotatedPanelRecord = panelDriverAttributeAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.isCancerAssociatedVariant).isFalse
        assertThat(annotatedVariant.evidence).isEqualTo(EMPTY_MATCH)
        assertThat(annotatedVariant.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(actinProteinEffect.NO_EFFECT)
        assertThat(annotatedVariant.isAssociatedWithDrugResistance).isFalse
        assertThat(annotatedVariant.driverLikelihood).isNull()
    }

    @Test
    fun `Should not annotate with evidence when no matches found`() {
        every { knownEventResolver.resolveForVariant(any()) } returns TestVariantAlterationFactory.createVariantAlteration(GENE)

        val panelRecord = panelRecordWith(VARIANT.copy(gene = "other gene"))
        val annotatedPanelRecord = panelDriverAttributeAnnotator.annotate(panelRecord)
        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        assertThat(annotatedPanelRecord.drivers.variants.first().evidence).isEqualTo(TestClinicalEvidenceFactory.createEmpty())

    }

    @Test
    fun `Should annotate fusion`() {
        every { knownEventResolver.resolveForFusion(any()) } returns KNOWN_FUSION

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalFusion())
        val annotatedPanelRecord = panelDriverAttributeAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.fusions).hasSize(1)
        val annotatedFusion = annotatedPanelRecord.drivers.fusions.first()

        assertThat(annotatedFusion.evidence).isEqualTo(EMPTY_MATCH)
        assertThat(annotatedFusion.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedFusion.isAssociatedWithDrugResistance).isTrue
    }

    @Test
    fun `Should annotate copy number`() {
        every { knownEventResolver.resolveForCopyNumber(any()) } returns AMPLIFICATION

        val panelRecord = panelRecordWith(TestMolecularFactory.createMinimalCopyNumber())
        val annotatedPanelRecord = panelDriverAttributeAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.copyNumbers).hasSize(1)
        val annotatedCopyNumber = annotatedPanelRecord.drivers.copyNumbers.first()

        assertThat(annotatedCopyNumber.evidence).isEqualTo(EMPTY_MATCH)
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