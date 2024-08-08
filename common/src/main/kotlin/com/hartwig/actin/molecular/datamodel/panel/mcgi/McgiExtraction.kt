package com.hartwig.actin.molecular.datamodel.panel.mcgi

import com.hartwig.actin.molecular.datamodel.panel.*
import java.time.LocalDate


data class McgiExtraction(
    override val panelType: String,
    override val date: LocalDate?,
    override val variants: List<PanelVariantExtraction>,
    override val fusions: List<PanelFusionExtraction>,
    override val amplifications: List<PanelAmplificationExtraction>,
    override val isMicrosatelliteUnstable: Boolean?,
    override val tumorMutationalBurden: Double?,
    override val skippedExons: List<PanelSkippedExonsExtraction> = emptyList(),
    override val extractionClass: String = McgiExtraction::class.java.simpleName
) : PanelExtraction {

    // TODO update these with skipped exon genes. or maybe refactor up to panel extraction?
    override fun testedGenes(): Set<String> {
        return (variants.map { it.gene } + amplifications.map { it.gene }).toSet()
    }

    override fun events(): Set<PanelEvent> {
        return (variants + amplifications).toSet()
    }
}
