package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.Displayable

enum class GenericPanelType(private val display: String) : Displayable {
    AVL("AvL Panel");

    override fun display(): String {
        return display
    }
}