package com.hartwig.actin.molecular.datamodel

interface Drivers<V : Variant, F : Fusion> {
    val variants: Set<V>
    val fusions: Set<F>
}