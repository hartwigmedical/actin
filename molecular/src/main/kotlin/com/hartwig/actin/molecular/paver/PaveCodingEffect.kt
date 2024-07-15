package com.hartwig.actin.molecular.paver

enum class PaveCodingEffect {
    NONSENSE_OR_FRAMESHIFT,
    SPLICE,
    MISSENSE,
    SYNONYMOUS,
    NONE;

    companion object {
        fun fromString(text: String): PaveCodingEffect {
            return try {
                valueOf(text)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown PAVE coding effect: $text")
            }
        }
    }
}