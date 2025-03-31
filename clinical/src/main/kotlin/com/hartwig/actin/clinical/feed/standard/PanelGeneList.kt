package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class PanelGeneList(private val panelRegexToGenes: Map<Regex, List<String>>) {

    fun listGenesForPanel(panelName: String): Set<String> {
        return panelRegexToGenes.filterKeys { it.containsMatchIn(panelName.lowercase()) }.values.flatten().toSet()
    }

    companion object {
        fun create(panelGeneListTsvPath: String): PanelGeneList {

            data class PanelGeneEntry(val testNameRegex: String, val gene: String)

            val entries = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(PanelGeneEntry::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
            return PanelGeneList(entries.readAll().groupBy(PanelGeneEntry::testNameRegex, PanelGeneEntry::gene).mapKeys { Regex(it.key) })
        }
    }
}