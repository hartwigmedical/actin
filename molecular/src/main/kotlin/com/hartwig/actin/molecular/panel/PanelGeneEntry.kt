package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelGeneSpecification
import com.hartwig.actin.datamodel.molecular.PanelTestSpecification
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

    fun toPanelTestSpecification(labConfigurations: LabConfigurations): PanelTestSpecification {
        val labs = labConfigurations[testName]
            ?: throw IllegalStateException("No lab configuration found for test '$testName'. Please add it to panel_details.tsv")
        return PanelTestSpecification(testName, versionDate, labs)
    }
}
