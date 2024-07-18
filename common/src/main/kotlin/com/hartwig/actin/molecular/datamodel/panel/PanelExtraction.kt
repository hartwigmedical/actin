package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

interface PanelExtraction {
    val panelType: String
    val tumorMutationalBurden: Double?
    val isMicrosatelliteUnstable: Boolean?
    val variants: List<PanelVariantExtraction>
    val amplifications: List<PanelAmplificationExtraction>
    val date: LocalDate?
    val extractionClass: String

    fun testedGenes(): Set<String>
    fun events(): Set<PanelEvent>
}