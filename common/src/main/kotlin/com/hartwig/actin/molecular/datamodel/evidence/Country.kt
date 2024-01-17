package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.actin.Displayable

enum class Country(private val display: String) : Displayable {
    NETHERLANDS("Netherlands"),
    BELGIUM("Belgium"),
    GERMANY("Germany"),
    OTHER("Other");

    override fun display(): String {
        return display
    }
}
