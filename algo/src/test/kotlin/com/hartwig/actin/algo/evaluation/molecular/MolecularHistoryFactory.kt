package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.molecular.datamodel.ArcherMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant

internal object MolecularHistoryFactory {

    fun emptyMolecularHistory(): MolecularHistory {
        return MolecularHistory(emptyList())
    }

    fun withArcherVariant(gene: String, hgvsCodingImpact: String): MolecularHistory {
        return MolecularHistory(molecularTests = listOf(
            ArcherMolecularTest(date = null, result = ArcherPanel(
                variants = listOf(ArcherVariant(gene = gene, hgvsCodingImpact = hgvsCodingImpact)),
                fusions = emptyList()
            ))
        ))
    }

    fun withArcherFusion(geneStart: String, geneEnd: String): MolecularHistory {
        return MolecularHistory(molecularTests = listOf(
            ArcherMolecularTest(date = null, result = ArcherPanel(
                variants = emptyList(),
                fusions = listOf(ArcherFusion(geneStart = geneStart, geneEnd = geneEnd))
            ))
        ))
    }

    fun withEmptyArcherPanel(): MolecularHistory {
        return MolecularHistory(molecularTests = listOf(
            ArcherMolecularTest(date = null, result = ArcherPanel(
                variants = emptyList(),
                fusions = emptyList()
            ))
        ))
    }
}