package com.hartwig.actin.molecular.priormoleculartest

import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantAnnotation
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.Knowledgebase
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EMPTY_MATCH = ActionabilityMatch(emptyList(), emptyList())
private const val POSITION = 123
private const val REF = "C"
private const val ALT = "G"
private const val CHROMOSOME = "1"

private val ARCHER_PANEL_WITH_VARIANT =
    ArcherPanel(variants = listOf(ArcherVariant(GENE, HGVS_CODING, ref = REF, alt = ALT, position = POSITION, chromosome = CHROMOSOME)))

private val VARIANT_MATCH_CRITERIA = VariantMatchCriteria(
    isReportable = true,
    gene = GENE,
    chromosome = CHROMOSOME,
    ref = REF,
    alt = ALT,
    position = POSITION,
)

private val ARCHER_FUSION = ArcherFusion(GENE)

class ArcherAnnotatorTest {

    private val evidenceDatabase = mockk<EvidenceDatabase> {
        every { evidenceForVariant(any()) } returns EMPTY_MATCH
        every { geneAlterationForVariant(any()) } returns null
    }
    private val annotator = ArcherAnnotator(evidenceDatabase)

    @Test
    fun `Should return empty annotation when no matches found`() {
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        assertThat(annotated.variants[0].annotation).isEqualTo(
            ArcherVariantAnnotation(
                evidence = ActionableEvidence(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN
            )
        )
    }

    @Test
    fun `Should carry forward fusions without annotation`() {
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT.copy(fusions = listOf(ARCHER_FUSION)))
        assertThat(annotated.fusions).containsExactly(ARCHER_FUSION)
    }

    @Test
    fun `Should annotate variants with evidence`() {
        every { evidenceDatabase.evidenceForVariant(VARIANT_MATCH_CRITERIA) } returns ActionabilityMatch(
            onLabelEvents = listOf(
                TestServeActionabilityFactory.geneBuilder().build().withSource(Knowledgebase.CKB_EVIDENCE).withLevel(EvidenceLevel.A)
                    .withDirection(EvidenceDirection.RESPONSIVE)
            ), offLabelEvents = emptyList()
        )
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        val annotation = annotated.variants[0].annotation
        assertThat(annotation).isEqualTo(
            ArcherVariantAnnotation(
                evidence = ActionableEvidence(approvedTreatments = setOf("")),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN
            )
        )
    }

    @Test
    fun `Should annotate variants with gene alteration`() {
        every { evidenceDatabase.geneAlterationForVariant(VARIANT_MATCH_CRITERIA) } returns TestServeKnownFactory.hotspotBuilder().build()
            .withGeneRole(com.hartwig.serve.datamodel.common.GeneRole.ONCO)
            .withProteinEffect(com.hartwig.serve.datamodel.common.ProteinEffect.GAIN_OF_FUNCTION)
        val annotated = annotator.annotate(ARCHER_PANEL_WITH_VARIANT)
        val annotation = annotated.variants[0].annotation
        assertThat(annotation).isEqualTo(
            ArcherVariantAnnotation(
                evidence = ActionableEvidence(),
                geneRole = GeneRole.ONCO,
                proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
            )
        )
    }
}