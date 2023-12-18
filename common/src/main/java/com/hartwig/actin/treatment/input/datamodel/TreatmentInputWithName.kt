package com.hartwig.actin.treatment.input.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TreatmentInputWithName() {
    abstract fun treatment(): TreatmentCategoryInput
    abstract fun name(): String?
}
