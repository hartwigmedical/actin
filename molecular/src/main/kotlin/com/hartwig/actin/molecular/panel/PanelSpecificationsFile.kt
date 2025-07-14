package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import java.io.File

object PanelSpecificationsFile {

    fun create(panelGeneListTsvPath: String, labConfigurations: LabConfigurations): PanelSpecifications {
        val entries = CsvMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }.readerFor(PanelGeneEntry::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))

        val grouped = entries.readAll().groupBy({ it.toPanelTestSpecification(labConfigurations) }, { it.toPanelGeneSpecification() })
        return PanelSpecifications(grouped)
    }
}