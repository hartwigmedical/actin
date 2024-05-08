package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.Displayable

interface PanelEvent : Displayable {

    fun impactsGene(gene: String): Boolean

    override fun display(): String
}