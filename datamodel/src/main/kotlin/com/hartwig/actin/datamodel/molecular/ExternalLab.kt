package com.hartwig.actin.datamodel.molecular

enum class ExternalLab(val displayName: String) {
    NKI("NKI-AvL"),
    EMC("Erasmus MC"),
    LUMC("LUMC"),
    ANY("any");

    companion object {
        fun fromString(value: String): ExternalLab? {
            return entries.find { it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) }
        }
    }
}