package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

enum class TestTarget {
    FUSION,
    NON_FUSION
}

data class TestedGene(val geneName: String, val targets: List<TestTarget>)

class PanelGeneList(private val panelRegexToGenes: Map<String, List<TestedGene>>) {

    fun listGenesForPanel(panelName: String): Set<TestedGene> {
        return panelRegexToGenes[panelName]?.toSet() ?: emptySet()
    }

    companion object {
        fun create(panelGeneListTsvPath: String): PanelGeneList {

            data class PanelGeneEntry(val testName: String, val gene: String, val fusion: Boolean, val anyNonFusion: Boolean)

            val entries = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(PanelGeneEntry::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t')).readValues<PanelGeneEntry>(File(panelGeneListTsvPath))
            return PanelGeneList(
                entries.readAll().groupBy(PanelGeneEntry::testName)
                    .mapValues {
                        it.value.map { g ->
                            TestedGene(
                                g.gene,
                                listOfNotNull(
                                    if (g.fusion) TestTarget.FUSION else null,
                                    if (g.anyNonFusion) TestTarget.NON_FUSION else null
                                )
                            )
                        }
                    })
        }
    }
}