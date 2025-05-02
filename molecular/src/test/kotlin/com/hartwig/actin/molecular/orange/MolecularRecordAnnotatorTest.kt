package com.hartwig.actin.molecular.orange

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestVariantAlterationFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.TestEvidenceDatabaseFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val VARIANT = TestVariantFactory.createMinimal()
private val VARIANT_MATCH_CRITERIA = VariantMatchCriteria(
    gene = VARIANT.gene,
    codingEffect = VARIANT.canonicalImpact.codingEffect,
    type = VARIANT.type,
    chromosome = VARIANT.chromosome,
    position = VARIANT.position,
    ref = VARIANT.ref,
    alt = VARIANT.alt,
    driverLikelihood = VARIANT.driverLikelihood,
    isReportable = VARIANT.isReportable
)

private val EMPTY_MATCH = TestClinicalEvidenceFactory.createEmpty()

private val HOTSPOT =
    TestVariantAlterationFactory.createVariantAlteration(VARIANT.gene, GeneRole.ONCO, ProteinEffect.GAIN_OF_FUNCTION, true, true)

private val NON_HOTSPOT =
    TestVariantAlterationFactory.createVariantAlteration(VARIANT.gene, GeneRole.ONCO, ProteinEffect.NO_EFFECT, false, false)

class MolecularRecordAnnotatorTest {

    private val annotator = MolecularRecordAnnotator(TestEvidenceDatabaseFactory.createProperDatabase())

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
    fun `Should annotate variant that is hotspot according to serve`() {
        val evidenceDatabase = mockk<EvidenceDatabase> {
            every { variantAlterationForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns HOTSPOT
            every { evidenceForVariant(any()) } returns EMPTY_MATCH
        }

        val annotated = MolecularRecordAnnotator(evidenceDatabase).annotateVariant(VARIANT)
        assertThat(annotated.isHotspot).isTrue()
        assertThat(annotated.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
        assertThat(annotated.proteinEffect.name).isEqualTo(HOTSPOT.proteinEffect.name)
        assertThat(annotated.geneRole.name).isEqualTo(HOTSPOT.geneRole.name)
    }

    @Test
    fun `Should annotate variant that is no hotspot according to serve`() {
        val evidenceDatabase = mockk<EvidenceDatabase> {
            every { variantAlterationForVariant(VARIANT_MATCH_CRITERIA.copy(driverLikelihood = null)) } returns NON_HOTSPOT
            every { evidenceForVariant(any()) } returns EMPTY_MATCH
        }

        val annotated = MolecularRecordAnnotator(evidenceDatabase).annotateVariant(VARIANT)
        assertThat(annotated.isHotspot).isEqualTo(VARIANT.isHotspot)
        assertThat(annotated.driverLikelihood).isEqualTo(VARIANT.driverLikelihood)
        assertThat(annotated.proteinEffect.name).isEqualTo(NON_HOTSPOT.proteinEffect.name)
        assertThat(annotated.geneRole.name).isEqualTo(NON_HOTSPOT.geneRole.name)
    }

    @Test
    fun `Should annotate driver likelihood on gene level`() {
        val variants = listOf(
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.HIGH),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.MEDIUM),
            TestVariantFactory.createMinimal().copy(driverLikelihood = DriverLikelihood.LOW),
            TestVariantFactory.createMinimal()
        )
        val output = annotator.annotateDriverLikelihood(variants)
        assertThat(output.all { it.driverLikelihood == DriverLikelihood.HIGH }).isTrue()
    }
}