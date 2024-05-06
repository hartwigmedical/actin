package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant

internal object MolecularHistoryFactory {

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherMolecularTest(
                    date = null, result = ArcherPanel(
                        variants = listOf(ArcherVariant(gene = gene, hgvsCodingImpact = hgvsCodingImpact)),
                        fusions = emptyList(),
                        skippedExons = emptyList()
                    )
                )
            )
        )
    }

    fun withArcherFusion(geneStart: String): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherMolecularTest(
                    date = null, result = ArcherPanel(
                        variants = emptyList(),
                        fusions = listOf(ArcherFusion(gene = geneStart)),
                        skippedExons = emptyList()
                    )
                )
            )
        )
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(
            molecularTests = listOf(
                ArcherMolecularTest(
                    date = null, result = ArcherPanel(
                        variants = emptyList(),
                        fusions = emptyList(),
                        skippedExons = emptyList()
                    )
                )
            )
        )
    }
}