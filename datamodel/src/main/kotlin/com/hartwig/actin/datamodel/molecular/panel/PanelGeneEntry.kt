package com.hartwig.actin.datamodel.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import java.time.LocalDate

data class PanelGeneEntry(
    val testName: String,
    val versionDate: LocalDate? = null,
    val gene: String,
    val mutation: Boolean,
    val amplification: Boolean,
    val deletion: Boolean,
    val fusion: Boolean
) {
    fun toPanelGeneSpecification(): PanelGeneSpecification {
        val targets = listOfNotNull(
            if (mutation) MolecularTestTarget.MUTATION else null,
            if (amplification) MolecularTestTarget.AMPLIFICATION else null,
            if (deletion) MolecularTestTarget.DELETION else null,
            if (fusion) MolecularTestTarget.FUSION else null,
        )
        if (targets.isEmpty()) throw IllegalStateException(
            "Targets for test $testName${versionDate?.let { " version $it" } ?: ""} and gene $gene are empty, a gene should be tested for at least one target. " +
                    "Please correct in the panel_specifications.tsv"
        )
        return PanelGeneSpecification(gene, targets)
    }
}