package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant

internal object MolecularHistoryFactory {

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                archerPanelRecord(
                    ArcherPanelExtraction(
                        variants = listOf(ArcherVariant(gene = gene, hgvsCodingImpact = hgvsCodingImpact))
                    )
                )
            )
        )
    }

    fun withArcherFusion(geneStart: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                archerPanelRecord(ArcherPanelExtraction(fusions = listOf(ArcherFusion(gene = geneStart))))
            )
        )
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(archerPanelRecord(ArcherPanelExtraction()))
        )
    }

    fun archerPanelRecord(extraction: ArcherPanelExtraction) = TestPanelRecordFactory.empty().copy(
        archerPanelExtraction = extraction
    )
}