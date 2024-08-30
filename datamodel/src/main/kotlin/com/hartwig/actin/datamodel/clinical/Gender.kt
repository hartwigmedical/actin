package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.Displayable

enum class Gender(private val display: String) : Displayable {
    MALE("Male"),
    FEMALE("Female");

    override fun display(): String {
        return display
    }
}
