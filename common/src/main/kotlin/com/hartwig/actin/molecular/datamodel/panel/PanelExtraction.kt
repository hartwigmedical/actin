package com.hartwig.actin.molecular.datamodel.panel

import java.time.LocalDate

interface PanelExtraction {
    val variants: List<PanelVariantExtraction>
    val date: LocalDate?

    fun testedGenes(): Set<String>
    fun events(): Set<PanelEvent>
}