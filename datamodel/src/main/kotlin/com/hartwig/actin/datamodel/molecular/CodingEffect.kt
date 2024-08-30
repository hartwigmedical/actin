package com.hartwig.actin.datamodel.molecular

import com.hartwig.actin.datamodel.Displayable

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
