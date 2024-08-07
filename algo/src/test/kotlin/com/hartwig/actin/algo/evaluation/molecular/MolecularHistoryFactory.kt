package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.*
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction

val PROPER_PANEL_VARIANT = Variant(
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
    ),
    otherImpacts = emptySet()
)

internal object MolecularHistoryFactory {

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                archerPanelRecord(
                    ArcherPanelExtraction(
                        variants = listOf(PanelVariantExtraction(gene = gene, hgvsCodingOrProteinImpact = hgvsCodingImpact))
                    )
                ).copy(
                    drivers = Drivers(
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
                archerPanelRecord(ArcherPanelExtraction(fusions = listOf(PanelFusionExtraction(geneStart, null))))
            )
        )
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(archerPanelRecord(ArcherPanelExtraction()))
        )
    }

    fun archerPanelRecord(extraction: ArcherPanelExtraction) = TestPanelRecordFactory.empty().copy(
        panelExtraction = extraction
    )
}