package com.hartwig.actin.molecular.datamodel.wgs.driver

import com.hartwig.actin.Displayable

enum class FusionDriverType(private val display: String) : Displayable {
    NONE("None"),
    KNOWN_PAIR("Known fusion"),
    KNOWN_PAIR_IG("IG known fusion"),
    KNOWN_PAIR_DEL_DUP("Known fusion"),
    PROMISCUOUS_3("3' promiscuous fusion"),
    PROMISCUOUS_5("5' promiscuous fusion"),
    PROMISCUOUS_BOTH("3' and 5' promiscuous fusion"),
    PROMISCUOUS_IG("IG promiscuous fusion"),
    PROMISCUOUS_ENHANCER_TARGET("Promiscuous enhancer target fusion");

    override fun display(): String {
        return display
    }
}
