package com.hartwig.actin.datamodel.molecular.evidence

import com.hartwig.actin.datamodel.Displayable

enum class Country(private val display: String) : Displayable {
    NETHERLANDS("Netherlands"),
    BELGIUM("Belgium"),
    GERMANY("Germany"),
    USA("United States"),
    UK("United Kingdom"),
    FRANCE("France"),
    OTHER("Other");

    override fun display(): String {
        return display
    }
}
