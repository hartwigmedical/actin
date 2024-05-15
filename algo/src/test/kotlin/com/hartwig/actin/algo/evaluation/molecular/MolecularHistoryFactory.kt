package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant

internal object MolecularHistoryFactory {

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherPanel(
                    variants = listOf(ArcherVariant(gene = gene, hgvsCodingImpact = hgvsCodingImpact))
                )
            )
        )
    }

    fun withArcherFusion(geneStart: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherPanel(fusions = listOf(ArcherFusion(gene = geneStart)))
            )
        )
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherPanel()
            )
        )
    }
}