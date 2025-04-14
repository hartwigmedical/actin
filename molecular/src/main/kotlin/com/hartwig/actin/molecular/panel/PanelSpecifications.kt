package com.hartwig.actin.molecular.panel

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.MolecularTestTarget
import com.hartwig.actin.datamodel.molecular.PanelGeneSpecification
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import java.io.File

object PanelSpecificationsFile {

    fun create(panelGeneListTsvPath: String): PanelSpecifications {

        data class PanelGeneEntry(val testName: String, val gene: String, val fusion: Boolean, val mutation: Boolean, val amp: Boolean, val del: Boolean)

        val entries = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(PanelGeneEntry::class.java)
            .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
        return PanelSpecifications(
            entries.readAll().groupBy(PanelGeneEntry::testName)
                .mapValues {
                    it.value.map { g ->
                        PanelGeneSpecification(
                            g.gene,
                            listOfNotNull(
                                if (g.fusion) MolecularTestTarget.FUSION else null,
                                if (g.mutation) MolecularTestTarget.MUTATION else null,
                                if (g.amp) MolecularTestTarget.AMPLIFICATION else null,
                                if (g.del) MolecularTestTarget.DELETION else null,
                            )
                        )
                    }
                })
    }
}