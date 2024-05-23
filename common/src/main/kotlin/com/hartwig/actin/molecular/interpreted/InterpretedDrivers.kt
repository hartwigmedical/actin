package com.hartwig.actin.molecular.interpreted

interface InterpretedDrivers<V : InterpretedVariant, F : InterpretedFusion> {
    val variants: Set<V>
    val fusions: Set<F>
}