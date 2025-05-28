package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolver
import com.hartwig.actin.molecular.evidence.known.TestKnownEventResolverFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val GENE = "gene 1"
private val VARIANT = TestVariantFactory.createMinimal()

private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()

private val HOTSPOT =
    TestVariantAlterationFactory.createVariantAlteration(VARIANT.gene, GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true, true)

private val NON_HOTSPOT =
    TestVariantAlterationFactory.createVariantAlteration(VARIANT.gene, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)

class MolecularRecordAnnotatorTest {

    private val annotator = MolecularRecordAnnotator(TestKnownEventResolverFactory.createProper())

    @Test
    fun `Should retain characteristics during annotation that are originally present`() {
        val annotated = annotator.annotate(TestMolecularFactory.createProperTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteStability?.evidence).isNotNull()
            assertThat(homologousRecombination?.evidence).isNotNull()
            assertThat(tumorMutationalBurden?.evidence).isNotNull()
            assertThat(tumorMutationalLoad?.evidence).isNotNull()
        }
    }

    @Test
    fun `Should not create characteristics during annotation that are originally missing`() {
        val annotated = annotator.annotate(TestMolecularFactory.createMinimalTestMolecularRecord())
        with(annotated.characteristics) {
            assertThat(microsatelliteStability).isNull()
            assertThat(homologousRecombination).isNull()
            assertThat(tumorMutationalBurden).isNull()
            assertThat(tumorMutationalLoad).isNull()
        }
    }

    @Test
    fun `Should annotate variant that is hotspot`() {
        val evidenceDatabase = mockk<KnownEventResolver> {
            every { resolveForVariant(VARIANT.copy(driverLikelihood = null)) } returns HOTSPOT
        }

        val annotated = MolecularRecordAnnotator(evidenceDatabase).annotateVariant(VARIANT)
        assertThat(annotated.isHotspot).isTrue()
        assertThat(annotated.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(annotated.proteinEffect.name).isEqualTo(HOTSPOT.proteinEffect.name)
        assertThat(annotated.geneRole.name).isEqualTo(HOTSPOT.geneRole.name)
    }

    @Test
    fun `Should annotate variant that is no hotspot`() {
        val evidenceDatabase = mockk<KnownEventResolver> {
            every { resolveForVariant(VARIANT.copy(driverLikelihood = null)) } returns NON_HOTSPOT
        }

        val annotated = MolecularRecordAnnotator(evidenceDatabase).annotateVariant(VARIANT)
        assertThat(annotated.isHotspot).isFalse()
        assertThat(annotated.driverLikelihood).isEqualTo(VARIANT.driverLikelihood)
        assertThat(annotated.proteinEffect.name).isEqualTo(NON_HOTSPOT.proteinEffect.name)
        assertThat(annotated.geneRole.name).isEqualTo(NON_HOTSPOT.geneRole.name)
    }

    @Test
    fun `Should reannotate driver likelihood on gene level`() {
        val variants = listOf(
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.HIGH, gene = GENE),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM, gene = GENE),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.LOW, gene = GENE),
        )
        val output = annotator.reannotateDriverLikelihood(variants)
        assertThat(output.all { it.driverLikelihood == DriverLikelihood.HIGH }).isTrue()
    }

    @Test
    fun `Should not reannotate driver likelihood when variant has driver likelihood null`() {
        val variants = listOf(
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.HIGH, gene = GENE),
            TestVariantFactory.createMinimal().copy(driverLikelihood = null, gene = GENE)
        )
        val output = annotator.reannotateDriverLikelihood(variants)
        assertThat(output[0].driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(output[1].driverLikelihood).isNull()
    }

    @Test
    fun `Should not reannotate driver likelihood for variants on different genes`() {
        val variants = listOf(
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.HIGH, gene = GENE),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM, gene = "other gene"),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM, gene = GENE),
        )
        val output = annotator.reannotateDriverLikelihood(variants)
        assertThat(output.filter { it.gene == GENE }.all { it.driverLikelihood == DriverLikelihood.HIGH }).isTrue()
        assertThat(output.filter { it.gene == "other gene" }.all { it.driverLikelihood == DriverLikelihood.MEDIUM }).isTrue()
    }
}