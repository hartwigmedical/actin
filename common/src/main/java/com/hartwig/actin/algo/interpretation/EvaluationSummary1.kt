package com.hartwig.actin.algo.interpretation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class EvaluationSummary {
    abstract fun count(): Int
    abstract fun warningCount(): Int
    abstract fun passedCount(): Int
    abstract fun failedCount(): Int
    abstract fun undeterminedCount(): Int
    abstract fun notEvaluatedCount(): Int
    abstract fun nonImplementedCount(): Int
}
