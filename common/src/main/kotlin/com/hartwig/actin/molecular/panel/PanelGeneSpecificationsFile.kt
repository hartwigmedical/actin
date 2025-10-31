package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.panel.PanelTestSpecification
import com.hartwig.actin.datamodel.molecular.panel.TestVersion
import com.hartwig.actin.molecular.filter.GeneFilter
import java.io.File

object PanelGeneSpecificationsFile {

    fun parseEntries(panelGeneListTsvPath: String): List<PanelGeneEntry> {
        return CsvMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }.readerFor(PanelGeneEntry::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
            .readAll()
    }

    fun create(panelGeneListTsvPath: String, geneFilter: GeneFilter): PanelSpecifications {
        return PanelSpecifications(
            geneFilter,
            parseEntries(panelGeneListTsvPath).groupBy(
                { PanelTestSpecification(it.testName, TestVersion(it.versionDate)) },
                { it.toPanelGeneSpecification() })
        )
    }
}
