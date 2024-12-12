package com.hartwig.actin.algo.evaluation

interface IcdMatches<T> {
    val fullMatches: List<T>
    val mainCodeMatchesWithUnknownExtension: List<T>
}