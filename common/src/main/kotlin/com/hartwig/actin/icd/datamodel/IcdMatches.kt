package com.hartwig.actin.icd.datamodel

data class IcdMatches<T> (
    val fullMatches: List<T>,
    val mainCodeMatchesWithUnknownExtension: List<T>
)