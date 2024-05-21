package com.hartwig.actin.molecular.datamodel.panel

interface Panel {
    fun testedGenes(): Set<String>
    fun variants(): List<PanelEvent>
    fun fusions(): List<PanelEvent>

    fun events(): List<PanelEvent> {
        return variants() + fusions()
    }
}