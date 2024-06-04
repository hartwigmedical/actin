package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.VariantEffect
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.datamodel.panel.PanelDrivers
import com.hartwig.actin.molecular.datamodel.panel.PanelVariant
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariantExtraction

val PROPER_PANEL_VARIANT = PanelVariant(
    chromosome = "7",
    position = 140453136,
    ref = "T",
    alt = "A",
    isReportable = true,
    event = "BRAF V600E",
    driverLikelihood = DriverLikelihood.HIGH,
    evidence = TestActionableEvidenceFactory.withApprovedTreatment("Vemurafenib"),
    gene = "",
    geneRole = GeneRole.ONCO,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    isAssociatedWithDrugResistance = true,
    type = VariantType.SNV,
    isHotspot = true,
    canonicalImpact = TranscriptImpact(
        transcriptId = "ENST00000288602",
        hgvsCodingImpact = "c.1799T>A",
        hgvsProteinImpact = "p.V600E",
        affectedCodon = 600,
        isSpliceRegion = false,
        effects = setOf(VariantEffect.MISSENSE),
        codingEffect = CodingEffect.MISSENSE,
        affectedExon = null
    )
)

internal object MolecularHistoryFactory {

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                archerPanelRecord(
                    ArcherPanelExtraction(
                        variants = listOf(ArcherVariantExtraction(gene = gene, hgvsCodingImpact = hgvsCodingImpact))
                    )
                ).copy(
                    drivers = PanelDrivers(
                        variants = setOf(
                            PROPER_PANEL_VARIANT.copy(
                                gene = gene,
                                canonicalImpact = PROPER_PANEL_VARIANT.canonicalImpact.copy(hgvsCodingImpact = hgvsCodingImpact)
                            )
                        )
                    )
                )
            )
        )
    }

    fun withArcherFusion(geneStart: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                archerPanelRecord(ArcherPanelExtraction(fusions = listOf(ArcherFusionExtraction(gene = geneStart))))
            )
        )
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(archerPanelRecord(ArcherPanelExtraction()))
        )
    }

    private fun archerPanelRecord(extraction: ArcherPanelExtraction) = TestPanelRecordFactory.empty().copy(
        testedGenes = extraction.testedGenes(),
        panelEvents = extraction.events()
    )
}