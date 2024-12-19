package com.hartwig.actin.util

object MapFunctions {

    fun <T, R> mergeMapsOfSets(mapsOfSets: List<Map<T, Set<R>>>): Map<T, Set<R>> {
        return mapsOfSets
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.flatten().toSet() }
    }
}