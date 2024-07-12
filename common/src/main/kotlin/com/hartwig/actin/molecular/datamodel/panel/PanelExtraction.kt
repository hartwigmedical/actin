package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

interface PanelExtraction {
    val panelType: String
    val tmb: Double?
    val msi: Boolean?
    val variants: List<PanelVariantExtraction>
    val amplifications: List<McgiAmplification>
    val date: LocalDate?

    fun testedGenes(): Set<String>
    fun events(): Set<PanelEvent>
}