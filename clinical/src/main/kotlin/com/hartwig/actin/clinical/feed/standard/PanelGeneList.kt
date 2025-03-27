package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class PanelGeneList(private val panelRegexToGenes: Map<Regex, List<String>>) {

    operator fun get(input: String): Set<String> {
        return panelRegexToGenes
            .filter { (regex, _) -> regex.containsMatchIn(input.lowercase()) }
            .flatMap { (_, value) -> value }
            .toSet()
    }

    companion object {
        fun create(panelGeneListTsvPath: String): PanelGeneList {

            class PanelGeneEntry(val testNameRegex: String, val gene: String)

            val entries = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(PanelGeneEntry::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
            return PanelGeneList(entries.readAll().groupBy { it.testNameRegex }.mapKeys { Regex(it.key) }
                .mapValues { it.value.map { e -> e.gene } })
        }
    }
}