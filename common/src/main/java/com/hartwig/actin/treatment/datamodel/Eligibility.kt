package com.hartwig.actin.treatment.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Eligibility {
    abstract fun references(): Set<CriterionReference?>
    abstract fun function(): EligibilityFunction
}
