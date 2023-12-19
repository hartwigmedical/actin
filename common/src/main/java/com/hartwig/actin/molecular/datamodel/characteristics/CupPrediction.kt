package com.hartwig.actin.molecular.datamodel.characteristics

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CupPrediction {
    abstract fun cancerType(): String
    abstract fun likelihood(): Double
    abstract fun snvPairwiseClassifier(): Double
    abstract fun genomicPositionClassifier(): Double
    abstract fun featureClassifier(): Double
}