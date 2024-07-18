package com.hartwig.actin.molecular.datamodel.panel.mcgi

import com.hartwig.actin.molecular.datamodel.panel.PanelAmplificationExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelEvent
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelVariantExtraction
import java.time.LocalDate


data class McgiExtraction(
    override val panelType: String,
    override val date: LocalDate?,
    override val variants: List<PanelVariantExtraction>,
    override val amplifications: List<PanelAmplificationExtraction>,
    override val isMicrosatelliteUnstable: Boolean?,
    override val tumorMutationalBurden: Double?,
    override val extractionClass: String = McgiExtraction::class.java.simpleName
) : PanelExtraction {
    override fun testedGenes(): Set<String> {
        return (variants.map { it.gene } + amplifications.map { it.gene }).toSet()
    }

    override fun events(): Set<PanelEvent> {
        return (variants + amplifications).toSet()
    }
}
