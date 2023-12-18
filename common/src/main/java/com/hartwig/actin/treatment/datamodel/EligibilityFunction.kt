package com.hartwig.actin.treatment.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class EligibilityFunction {
    abstract fun rule(): EligibilityRule
    abstract fun parameters(): List<Any?>
}
