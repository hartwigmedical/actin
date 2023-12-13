package com.hartwig.actin.algo.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Evaluation {
    abstract fun result(): EvaluationResult
    abstract fun recoverable(): Boolean
    abstract fun inclusionMolecularEvents(): Set<String?>
    abstract fun exclusionMolecularEvents(): Set<String?>
    abstract fun passSpecificMessages(): Set<String?>
    abstract fun passGeneralMessages(): Set<String?>
    abstract fun warnSpecificMessages(): Set<String?>
    abstract fun warnGeneralMessages(): Set<String?>
    abstract fun undeterminedSpecificMessages(): Set<String?>
    abstract fun undeterminedGeneralMessages(): Set<String?>
    abstract fun failSpecificMessages(): Set<String?>
    abstract fun failGeneralMessages(): Set<String?>
}
