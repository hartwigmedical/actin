package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.serve.datamodel.molecular.common.GeneAlteration
import com.hartwig.serve.datamodel.molecular.fusion.KnownFusion
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.actin.datamodel.molecular.driver.GeneRole as actinGeneRole
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect as actinProteinEffect
import com.hartwig.serve.datamodel.molecular.common.GeneRole as ServeGeneRole
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect as serveProteinEffect

private val ACTIONABILITY_MATCH_FOR_VARIANT = mockk<ClinicalEvidence>()

private val ACTIONABILITY_MATCH_FOR_FUSION = mockk<ClinicalEvidence>()
private val KNOWN_FUSION = mockk<KnownFusion> {
    every { proteinEffect() } returns serveProteinEffect.GAIN_OF_FUNCTION
    every { associatedWithDrugResistance() } returns true
}

private val ACTIONABILITY_MATCH_FOR_COPY_NUMBER = mockk<ClinicalEvidence>()
private val GENE_ALTERATION = mockk<GeneAlteration> {
    every { geneRole() } returns ServeGeneRole.ONCO
    every { proteinEffect() } returns serveProteinEffect.GAIN_OF_FUNCTION
    every { associatedWithDrugResistance() } returns true
}

private const val ALT = "T"
private const val REF = "G"
private const val TRANSCRIPT = "transcript"
private const val GENE_ID = "gene_id"
private const val OTHER_TRANSCRIPT = "other_transcript"
private const val OTHER_GENE = "other_gene"
private const val OTHER_GENE_ID = "other_gene_id"
private const val OTHER_GENE_TRANSCRIPT = "other_gene_transcript"
private const val CHROMOSOME = "1"
private const val POSITION = 1
private const val HGVS_PROTEIN_3LETTER = "p.Met1Leu"
private const val HGVS_PROTEIN_1LETTER = "p.M1L"

private val VARIANT = Variant(
    chromosome = CHROMOSOME,
    position = POSITION,
    ref = REF,
    alt = ALT,
    type = VariantType.SNV,
    variantAlleleFrequency = null,
    canonicalImpact = TranscriptVariantImpact(
        transcriptId = TRANSCRIPT,
        codingEffect = CodingEffect.MISSENSE,
        hgvsCodingImpact = HGVS_CODING,
        hgvsProteinImpact = HGVS_PROTEIN_1LETTER,
        isSpliceRegion = false,
        affectedExon = 1,
        affectedCodon = 1,
        effects = emptySet()
    ),
    otherImpacts = emptySet(),
    isHotspot = true,
    isReportable = true,
    event = "$GENE M1L",
    driverLikelihood = null,
    evidence = ClinicalEvidence(emptySet(), emptySet()),
    gene = GENE,
    geneRole = GeneRole.ONCO,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    isAssociatedWithDrugResistance = true
)

class PanelEvidenceAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns ACTIONABILITY_MATCH_FOR_VARIANT
        every { geneAlterationForVariant(any()) } returns GENE_ALTERATION

        every { evidenceForFusion(any()) } returns ACTIONABILITY_MATCH_FOR_FUSION
        every { lookupKnownFusion(any()) } returns KNOWN_FUSION

        every { evidenceForCopyNumber(any()) } returns ACTIONABILITY_MATCH_FOR_COPY_NUMBER
        every { geneAlterationForCopyNumber(any()) } returns GENE_ALTERATION
    }

    // TODO unfortunate, this is intermediate and needed for the driver likelihood model, possible to refactor?
    val VARIANT_WITH_GENE_ALTERATION = VARIANT.copy(
        geneRole = GeneRole.ONCO,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        isAssociatedWithDrugResistance = true,
        isHotspot = false,  // TODO check how this get set
    )

    private val geneDriverLikelihoodModel = mockk<GeneDriverLikelihoodModel> {
        every { evaluate(any(), any(), any()) } returns null
        every {
            evaluate(GENE, GeneRole.ONCO, listOf(VARIANT_WITH_GENE_ALTERATION))
        } returns 0.9
    }

    private val panelEvidenceAnnotator = PanelEvidenceAnnotator(evidenceDatabase, geneDriverLikelihoodModel)

    @Test
    fun `Should annotate variant`() {
        val panelRecord = panelRecordWith(VARIANT)
        val annotatedPanelRecord = panelEvidenceAnnotator.annotate(panelRecord)

        assertThat(annotatedPanelRecord.drivers.variants).hasSize(1)
        val annotatedVariant = annotatedPanelRecord.drivers.variants.first()
        assertThat(annotatedVariant.evidence).isEqualTo(ACTIONABILITY_MATCH_FOR_VARIANT)
        assertThat(annotatedVariant.geneRole).isEqualTo(actinGeneRole.ONCO)
        assertThat(annotatedVariant.proteinEffect).isEqualTo(actinProteinEffect.GAIN_OF_FUNCTION)
        assertThat(annotatedVariant.isAssociatedWithDrugResistance).isTrue
        assertThat(annotatedVariant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
    }

    @Test
    fun `Should annotate fusion`() {
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