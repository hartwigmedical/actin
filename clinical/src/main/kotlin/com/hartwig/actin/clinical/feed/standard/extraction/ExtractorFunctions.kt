package com.hartwig.actin.clinical.feed.standard.extraction

inline fun <reified T : Enum<T>> enumeratedInput(input: String) =
    enumValues<T>().firstOrNull { it.name == input.uppercase().replace(" ", "_") } ?: { enumValueOf<T>("OTHER") }
