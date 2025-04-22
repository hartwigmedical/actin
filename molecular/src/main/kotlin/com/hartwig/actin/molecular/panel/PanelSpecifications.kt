package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelGeneSpecification
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import java.io.File

object PanelSpecificationsFile {

    fun create(panelGeneListTsvPath: String): PanelSpecifications {

        data class PanelGeneEntry(
            val testName: String,
            val gene: String,
            val fusion: Boolean,
            val mutation: Boolean,
            val amplification: Boolean,
            val deletion: Boolean
        ) {
            fun toPanelGeneSpecification(): PanelGeneSpecification {
                val targets = listOfNotNull(
                    if (fusion) MolecularTestTarget.FUSION else null,
                    if (mutation) MolecularTestTarget.MUTATION else null,
                    if (amplification) MolecularTestTarget.AMPLIFICATION else null,
                    if (deletion) MolecularTestTarget.DELETION else null,
                )
                if (targets.isEmpty()) throw IllegalStateException(
                    "Targets for test $testName and gene $gene are empty, a gene should be tested for at least one target. " +
                            "Please correct in the panel_specifications.tsv"
                )
                return PanelGeneSpecification(gene, targets)
            }
        }

        val entries = CsvMapper().apply {
            registerModule(KotlinModule.Builder().build())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }.readerFor(PanelGeneEntry::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
        return PanelSpecifications(
            entries.readAll().groupBy(PanelGeneEntry::testName, PanelGeneEntry::toPanelGeneSpecification)
        )
    }
}