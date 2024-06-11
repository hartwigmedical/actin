package com.hartwig.actin.molecular.datamodel.panel.generic

import com.hartwig.actin.Displayable

enum class GenericPanelType(private val display: String) : Displayable {
    AVL("AvL panel"),
    FREE_TEXT("Freetext curation");

    override fun display(): String {
        return display
    }
}