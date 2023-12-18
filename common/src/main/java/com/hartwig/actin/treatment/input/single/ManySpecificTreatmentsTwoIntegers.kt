package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ManySpecificTreatmentsTwoIntegers() {
    abstract fun integer1(): Int
    abstract fun integer2(): Int
    abstract fun treatments(): List<Treatment?>
}
