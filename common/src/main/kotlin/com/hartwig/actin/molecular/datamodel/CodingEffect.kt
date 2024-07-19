package com.hartwig.actin.molecular.datamodel

import com.hartwig.actin.Displayable

enum class CodingEffect(private val display: String) : Displayable {
    NONSENSE_OR_FRAMESHIFT("Nonsense/Frameshift"),
    SPLICE("Splice"),
    MISSENSE("Missense"),
    SYNONYMOUS("Synonymous"),
    NONE("None");

    override fun display(): String {
        return display
    }
}
