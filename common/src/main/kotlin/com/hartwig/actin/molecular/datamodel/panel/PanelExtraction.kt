package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

interface PanelExtraction {
    val tmb: Double?
    val msi: Boolean?
    val variants: List<PanelVariantExtraction>
    val date: LocalDate?

    fun testedGenes(): Set<String>
    fun events(): Set<PanelEvent>
}